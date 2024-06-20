package net.slqmy.parrot_mail.parrot.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.slqmy.parrot_mail.parrot.MailParrot;
import net.slqmy.parrot_mail.parrot.journey.JourneyData;
import org.bukkit.entity.Player;

public final class FlyToPlayerGoal extends Goal {
    private final MailParrot mailParrot;
    private final JourneyData journeyData;

    private final Player targetPlayer;
    private final double speed;

    private final org.bukkit.entity.Parrot bukkitParrot;
    private final net.minecraft.world.entity.animal.Parrot nmsParrot;

    private int timeToRecalculatePath;
    private boolean reachedTarget;

    public FlyToPlayerGoal(MailParrot parrot, Player player, double speed) {
        this.mailParrot = parrot;
        this.journeyData = parrot.getJourneyData();

        this.targetPlayer = player;
        this.speed = speed;

        this.bukkitParrot = parrot.getParrot();
        this.nmsParrot = parrot.getNmsParrot();

        this.timeToRecalculatePath = 0;
    }

    @Override
    public boolean canUse() {
        return existsAndIsAbleToMove() &&
               mailParrot.hasBundle() &&
               mailParrot.hasGuideItem() &&
               journeyData.onJourney();
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() && !reachedTarget;
    }

    @Override
    public void start() {
        if (bukkitParrot.isInsideVehicle()) {
            bukkitParrot.leaveVehicle();
        }

        nmsParrot.getNavigation().moveTo(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), speed);
        super.start();
    }

    @Override
    public void stop() {
        nmsParrot.getNavigation().stop();
        mailParrot.reapplyDefaultAi();

        super.stop();
    }

    @Override
    public void tick() {
        if (nmsParrot.getNavigation().isDone()) {
            reachedTarget = true;
        }

        if (timeToRecalculatePath-- <= 0) {
            nmsParrot.getNavigation().moveTo(
                targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ(), speed);

            this.timeToRecalculatePath = 20;
        }

        journeyData.fakeLocation(bukkitParrot.getLocation());
        journeyData.velocity(targetPlayer.getLocation().toVector()
            .subtract(bukkitParrot.getLocation().toVector())
            .normalize().multiply(speed));

        super.tick();
    }

    private boolean existsAndIsAbleToMove() {
        return bukkitParrot != null && bukkitParrot.isValid() &&
            !bukkitParrot.isSitting() &&
            !bukkitParrot.isDancing() &&
            !bukkitParrot.isFrozen() &&
            !bukkitParrot.isLoveMode() &&
            !bukkitParrot.isLeashed();
    }
}
