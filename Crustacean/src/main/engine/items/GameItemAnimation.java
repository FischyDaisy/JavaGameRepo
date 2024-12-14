package main.engine.items;

public final class GameItemAnimation {
    private int animationIdx;
    private int currentFrame;
    private boolean started;
    private boolean loaded;

    private int animModelIdx;

    public final int maxFrames;

    public GameItemAnimation(boolean started, int animationIdx, int currentFrame, int maxFrames) {
        this(started, false, animationIdx, 0, currentFrame, maxFrames);
    }

    public GameItemAnimation(boolean started, boolean loaded, int animationIdx, int animModelIdx, int currentFrame, int maxFrames) {
        this.started = started;
        this.loaded = loaded;
        this.animationIdx = animationIdx;
        this.animModelIdx = animModelIdx;
        this.currentFrame = currentFrame;
        this.maxFrames = maxFrames;
    }

    public int getAnimationIdx() {
        return animationIdx;
    }

    public void setAnimationIdx(int animationIdx) {
        this.animationIdx = animationIdx;
    }

    public int getAnimModelIdx() {
        return animModelIdx;
    }

    public void setAnimModelIdx(int animModelIdx) {
        this.animModelIdx = animModelIdx;
    }

    public int getCurrentFrame() {
        return currentFrame;
    }

    public void setCurrentFrame(int currentFrame) {
        this.currentFrame = currentFrame;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void toggle() {
        started = !started;
    }
}
