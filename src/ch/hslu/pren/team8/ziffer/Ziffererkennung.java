package ch.hslu.pren.team8.ziffer;

import org.opencv.core.Core;

/**
 * Created by gebs on 3/3/17.
 */
public class Ziffererkennung {

    private final boolean useCamera = false;

    public Ziffererkennung() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public void Start() {


    }


}
