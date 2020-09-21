package sample.ide.codeEditor;

import javafx.animation.Animation.Status;
import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * Scrollpane with kinda smooth transition scrolling.
 *
 * @author Matt
 */
public class SmoothishScrollpane extends ScrollPane {
    private final static int TRANSITION_DURATION = 200;
    private final static double BASE_MODIFIER = 1;

    /**
     * @param content
     *            Item to be wrapped in the scroll pane.
     */
    public SmoothishScrollpane(Node content) {
        // ease-of-access for inner class
        ScrollPane scroll = this;
        // set content in a wrapper
        VBox wrapper = new VBox(content);
        setContent(wrapper);
        // add scroll handling to wrapper
        wrapper.setOnScroll(new EventHandler<>() {
            private SmoothishTransition transition;

            @Override
            public void handle(ScrollEvent event) {
                double deltaY = BASE_MODIFIER * event.getDeltaY();
                double width = scroll.getContent().getBoundsInLocal().getWidth();
                double vvalue = scroll.getVvalue();
                Interpolator interp = Interpolator.LINEAR;
                transition = new SmoothishTransition(transition, deltaY) {
                    @Override
                    protected void interpolate(double frac) {
                        double x = interp.interpolate(vvalue, vvalue + -deltaY * getMod() / width, frac);
                        scroll.setVvalue(x);
                    }
                };
                transition.play();
            }
        });
    }

    /**
     * @param t
     *            Transition to check.
     * @return {@code true} if transition is playing.
     */
    private static boolean playing(Transition t) {
        return t.getStatus() == Status.RUNNING;
    }

    /**
     * @param d1
     *            Value 1
     * @param d2
     *            Value 2.
     * @return {@code true} if values signs are matching.
     */
    private static boolean sameSign(double d1, double d2) {
        return (d1 > 0 && d2 > 0) || (d1 < 0 && d2 < 0);
    }

    /**
     * Transition with varying speed based on previously existing transitions.
     *
     * @author Matt
     */
    abstract static class SmoothishTransition extends Transition {
        private final double mod;
        private final double delta;

        public SmoothishTransition(SmoothishTransition old, double delta) {
            setCycleDuration(Duration.millis(TRANSITION_DURATION));
            setCycleCount(0);
            // if the last transition was moving in the same direction, and is still playing
            // then increment the modifier. This will boost the distance, thus looking faster
            // and seemingly consecutive.
            if (old != null && sameSign(delta, old.delta) && playing(old)) {
                mod = old.getMod() + 1;
            } else {
                mod = 1;
            }
            this.delta = delta;
        }

        public double getMod() {
            return mod;
        }

        @Override
        public void play() {
            super.play();
            // Even with a linear interpolation, startup is visibly slower than the middle.
            // So skip a small bit of the animation to keep up with the speed of prior
            // animation. The value of 10 works and isn't noticeable unless you really pay
            // close attention. This works best on linear but also is decent for others.
            if (getMod() > 1) {
                jumpTo(getCycleDuration().divide(10));
            }
        }
    }
}
