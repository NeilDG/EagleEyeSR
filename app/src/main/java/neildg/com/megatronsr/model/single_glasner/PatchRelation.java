package neildg.com.megatronsr.model.single_glasner;

/**
 * Created by NeilDG on 5/10/2016.
 */
public class PatchRelation {
    private final static String TAG = "PatchRelation";

    private PatchAttribute lrAttrib;
    private PatchAttribute hrAttrib;
    private double similarity;

    public PatchRelation(PatchAttribute lrAttrib, PatchAttribute hrAttrib, double similarity) {
        this.lrAttrib = lrAttrib;
        this.hrAttrib = hrAttrib;
        this.similarity = similarity;
    }

    public PatchAttribute getLrAttrib() {
        return this.lrAttrib;
    }

    public PatchAttribute getHrAttrib() {
        return this.hrAttrib;
    }

    public double  getSimilarity() {
        return this.similarity;
    }
}
