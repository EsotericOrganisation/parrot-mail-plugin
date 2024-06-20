package net.slqmy.parrot_mail.parrot.ai;

import net.minecraft.world.entity.ai.goal.Goal;
import net.slqmy.parrot_mail.parrot.MailParrot;
import net.slqmy.parrot_mail.parrot.journey.JourneyData;
import org.bukkit.Location;

public final class FlyToLocationGoal extends Goal {
    private final MailParrot mailParrot;
    private final JourneyData journeyData;

    private final Location target;
    private final double speed;

    private final org.bukkit.entity.Parrot bukkitParrot;
    private final net.minecraft.world.entity.animal.Parrot nmsParrot;

    private boolean reachedTarget;

    public FlyToLocationGoal(MailParrot parrot, Location location, double speed) {
        this.mailParrot = parrot;
        this.journeyData = parrot.getJourneyData();

        this.target = location;
        this.speed = speed;

        this.bukkitParrot = parrot.getParrot();
        this.nmsParrot = parrot.getNmsParrot();
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

        nmsParrot.getNavigation().moveTo(target.getX(), target.getY(), target.getZ(), speed);
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

        journeyData.fakeLocation(bukkitParrot.getLocation());
        journeyData.velocity(target.toVector()
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
