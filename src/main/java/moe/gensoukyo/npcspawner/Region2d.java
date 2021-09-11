package moe.gensoukyo.npcspawner;

/**
 * @author SQwatermark
 */
@Deprecated
public class Region2d implements Cloneable {

    public Vec2d vec1;
    public Vec2d vec2;

    public Region2d(double pos1x, double pos1y, double pos2x, double pos2y) {
        this.vec1 = new Vec2d(pos1x, pos1y);
        this.vec2 = new Vec2d(pos2x, pos2y);
        this.fixPos();
    }

    public Region2d(Vec2d vec1, Vec2d vec2) {
        this.vec1= vec1;
        this.vec2 = vec2;
        this.fixPos();
    }

    @Override
    public Region2d clone() {
        return new Region2d(vec1, vec2);
    }

    public boolean isVecInRegion(Vec2d vec2d) {
        return vec2d.x > this.vec1.x && vec2d.x < this.vec2.x && vec2d.y > this.vec1.y && vec2d.y < this.vec2.y;
    }

    public boolean isCoincideWith(Region2d region) {
        Vec2d a1 = this.vec1;
        Vec2d a2 = this.vec2;
        Vec2d b1 = region.vec1;
        Vec2d b2 = region.vec2;
        return a2.x > b1.x && a1.x < b2.x && a1.y < b2.y && a2.y > b1.y;
    }

    private void fixPos() {
        double tx = vec1.x, ty = vec1.y;
        if (tx > vec2.x) {
            vec1.x = vec2.x;
            vec2.x = tx;
        }
        if (ty > vec2.y) {
            vec1.y = vec2.y;
            vec2.y = ty;
        }
    }

}
