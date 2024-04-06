package info.loenwind.enderioaddons.machine.niard;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;

import org.lwjgl.opengl.GL11;

import com.enderio.core.client.render.BoundingBox;
import com.enderio.core.client.render.CubeRenderer;
import com.enderio.core.client.render.RenderUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FluidRendererNiard extends TileEntitySpecialRenderer {

    @Override
    public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTick) {
        renderTankFluid((TileNiard) te, (float) x, (float) y, (float) z);
    }

    public static void renderTankFluid(@Nullable TileNiard niard, float x, float y, float z) {
        if (niard == null || niard.tank.getFluid() == null || niard.tank.getFluidAmount() <= 0) {
            return;
        }
        IIcon icon_fluid = niard.tank.getFluid()
            .getFluid()
            .getStillIcon();
        if (icon_fluid == null) {
            return;
        }

        float wscale = 0.98f;
        float yScale = 6 / 16f * niard.tank.getFilledRatio();
        BoundingBox bb = BoundingBox.UNIT_CUBE.scale(wscale, yScale, wscale);
        bb = bb.translate(0, -(1 - yScale) / 2 + 0.01f, 0);

        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        RenderUtil.bindBlockTexture();

        Tessellator.instance.startDrawingQuads();
        Tessellator.instance.addTranslation(x, y, z);
        CubeRenderer.get().render(bb, icon_fluid);
        Tessellator.instance.addTranslation(-x, -y, -z);
        Tessellator.instance.draw();

        GL11.glPopAttrib();
    }

}
