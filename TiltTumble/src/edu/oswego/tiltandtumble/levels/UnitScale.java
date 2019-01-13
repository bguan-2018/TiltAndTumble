package edu.oswego.tiltandtumble.levels;

public class UnitScale {

    private final float metersPerPixel;

    public UnitScale(float metersPerPixel) {
        this.metersPerPixel = metersPerPixel;
    }

    public float getScale() {
        return metersPerPixel;
    }

    public float metersToPixels(float meters) {
        return meters / metersPerPixel;
    }

    public float pixelsToMeters(float pixels) {
        return pixels * metersPerPixel;
    }
}
