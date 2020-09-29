package tmw.me.com.ide.fileTreeView;

import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.ImageTranscoder;

import java.awt.image.BufferedImage;

/**
 * This is a utility class for creating buffered images from svgs using batik.
 */
public class BufferedImageTranscoder extends ImageTranscoder {

    private BufferedImage img = null;

    @Override
    public BufferedImage createImage(int width, int height) {
        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public void writeImage(BufferedImage img, TranscoderOutput to) {
        this.img = img;
    }

    public BufferedImage getBufferedImage() {
        return img;
    }

}
