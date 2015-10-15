package info.loenwind.enderioaddons.machine.pmon;

import static info.loenwind.autosave.annotations.Store.StoreFor.ITEM;
import static info.loenwind.autosave.annotations.Store.StoreFor.SAVE;
import static info.loenwind.enderioaddons.machine.pmon.PacketPMon.requestUpdate;
import info.loenwind.autosave.annotations.Storable;
import info.loenwind.autosave.annotations.Store;
import info.loenwind.enderioaddons.baseclass.TileEnderIOAddons;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.EnderIO;
import crazypants.enderio.conduit.AbstractConduitNetwork;
import crazypants.enderio.conduit.ConduitUtil;
import crazypants.enderio.conduit.power.IPowerConduit;
import crazypants.enderio.conduit.power.NetworkPowerManager;
import crazypants.enderio.conduit.power.PowerConduitNetwork;
import crazypants.enderio.machine.ContinuousTask;
import crazypants.enderio.machine.IMachineRecipe;
import crazypants.enderio.machine.IPoweredTask;
import crazypants.enderio.machine.SlotDefinition;
import crazypants.enderio.network.PacketHandler;
import crazypants.enderio.power.BasicCapacitor;

@Storable
public class TilePMon extends TileEnderIOAddons {

  protected @Store({ SAVE, ITEM }) StatCollector stats10s = new StatCollector(2);
  protected @Store({ SAVE, ITEM }) StatCollector stats01m = new StatCollector(12);
  protected @Store({ SAVE, ITEM }) StatCollector stats10m = new StatCollector(120);
  protected @Store({ SAVE, ITEM }) StatCollector stats01h = new StatCollector(720);
  protected @Store({ SAVE, ITEM }) StatCollector stats06h = new StatCollector(7200);
  protected @Store({ SAVE, ITEM }) StatCollector stats24h = new StatCollector(17280);
  protected @Store({ SAVE, ITEM }) StatCollector stats07d = new StatCollector(120960);

  protected StatCollector[] stats = { stats10s, stats01m, stats10m, stats01h, stats06h, stats24h, stats07d };

  public TilePMon() {
    super(new SlotDefinition(0, 0, 1));
  }

  @Override
  public String getMachineName() {
    return BlockPMon.ModObject_blockPMon.unlocalisedName;
  }

  @Override
  protected boolean isMachineItemValidForSlot(int i, @Nullable ItemStack item) {
    return false;
  }

  // tick goes in here
  @Override
  protected boolean checkProgress(boolean redstoneChecksPassed) {
    return doTick();
  }

  protected boolean doTick() {
    usePower();
    NetworkPowerManager pm = getPowerManager();
    if (pm != null) {
      final int capPower = logSrqt2(pm.getPowerInCapacitorBanks());
      for (StatCollector statCollector : stats) {
        statCollector.addValue(capPower);
      }
    }
    return false;
  }
  
  private static final long bit62 = Integer.MAX_VALUE;
  private static final long bit63 = bit62 * 2;

  private static int logSrqt2(long value) {
    if (value <= 0) {
      return 0;
    } else if (value >= bit63) {
      return 63;
    } else if (value >= bit62) {
      return 62;
    }
    for (int i = 30; i >= 0; i--) {
      if ((value & (1 << i)) != 0) {
        if (i == 0) {
          return 1;
        }
        if ((value & (1 << (i - 1))) != 0) {
          return i * 2 + 1;
        }
        return i * 2;
      }
    }
    return 0;
  }

  private NetworkPowerManager getPowerManager() {
    for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
      IPowerConduit con = ConduitUtil.getConduit(worldObj, this, dir, IPowerConduit.class);
      if (con != null) {
        AbstractConduitNetwork<?, ?> n = con.getNetwork();
        if (n instanceof PowerConduitNetwork) {
          NetworkPowerManager pm = ((PowerConduitNetwork) n).getPowerManager();
          if (pm != null) {
            return pm;
          }
        }
      }
    }
    return null;
  }

  @Override
  protected IPoweredTask createTask(NBTTagCompound taskTagCompound) {
    return new ContinuousTask(getPowerUsePerTick());
  }

  @Override
  protected IPoweredTask createTask(IMachineRecipe nextRecipe, float chance) {
    return createTask(null);
  }

  @Override
  public void onCapacitorTypeChange() {
    switch (getCapacitorType()) {
    case BASIC_CAPACITOR:
      setCapacitor(new BasicCapacitor(100, 10000, 10));
      break;
    case ACTIVATED_CAPACITOR:
      setCapacitor(new BasicCapacitor(200, 50000, 10));
      break;
    case ENDER_CAPACITOR:
      setCapacitor(new BasicCapacitor(400, 100000, 10));
      break;
    }
    currentTask = createTask(null);
  }

  @SideOnly(Side.CLIENT)
  private long[] lastUpdateRequest = new long[stats.length];

  @SideOnly(Side.CLIENT)
  public StatCollector getStatCollector(int id) {
    if (id < 0 || id >= stats.length) {
      return null;
    }
    long now = EnderIO.proxy.getTickCount();
    if (lastUpdateRequest[id] < now) {
      lastUpdateRequest[id] = now + 10;
      PacketHandler.INSTANCE.sendToServer(requestUpdate(this, id));
    }
    return stats[id];
  }

}