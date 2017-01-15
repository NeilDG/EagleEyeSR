package neildg.com.eagleeyesr.metrics;

public class TimeMeasure{

    private long timeStart = 0;
    private long timeEnd = 0;

    public TimeMeasure() {

    }

    public void timeStart() {
        this.timeStart = System.currentTimeMillis();
    }

    public void timeEnd() {
        this.timeEnd = System.currentTimeMillis();
    }

    public long getDeltaDifference() {
        return (this.timeEnd - this.timeStart);
    }

    public float getDeltaSeconds() {
        return (this.timeEnd - this.timeStart) / 1000.0f;

}
}
