package edu.oswego.tiltandtumble;

public class Settings {
    private boolean useDpad = false;
    private boolean debugRender = false;

    public boolean isUseDpad() {
        return useDpad;
    }
    public void setUseDpad(boolean useDpad) {
        this.useDpad = useDpad;
    }
    public boolean isDebugRender() {
        return debugRender;
    }
    public void setDebugRender(boolean debugRender) {
        this.debugRender = debugRender;
    }
}
