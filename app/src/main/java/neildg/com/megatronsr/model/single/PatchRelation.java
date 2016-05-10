package neildg.com.megatronsr.model.single;

/**
 * Created by NeilDG on 5/10/2016.
 */
public class PatchRelation {
    private final static String TAG = "PatchRelation";

    private PatchAttribute lrAttrib;
    private PatchAttribute hrAttrib;

    public PatchRelation(PatchAttribute lrAttrib, PatchAttribute hrAttrib) {
        this.lrAttrib = lrAttrib;
        this.hrAttrib = hrAttrib;
    }
}
