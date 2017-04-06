package ch.hslu.pren.team8.ziffer;

import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.*;

import static org.opencv.features2d.DescriptorMatcher.FLANNBASED;

/**
 * Created by gebs on 3/31/17.
 */
@Deprecated
public class SVMBow {
    private final int CLUSTER_COUNT = 1000;
    DescriptorMatcher matcher = DescriptorMatcher.create(FLANNBASED);
    DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.BRISK);
}
