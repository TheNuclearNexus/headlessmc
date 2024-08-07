package me.earth.headlessmc.launcher.command;

import lombok.CustomLog;
import me.earth.headlessmc.api.HeadlessMc;
import me.earth.headlessmc.api.command.CommandException;
import me.earth.headlessmc.command.AbstractCommand;
import me.earth.headlessmc.logging.LogLevelUtil;

import java.util.Locale;
import java.util.logging.Level;

import static me.earth.headlessmc.logging.LogLevelUtil.getLevels;

@CustomLog
public class LogLevelCommand extends AbstractCommand {
    public LogLevelCommand(HeadlessMc ctx) {
        super(ctx, "loglevel", "Set the loglevel of HeadlessMC's logger.");
        args.put("<level>", "One of " + getLevels() + ". Decides how much" +
            " log output you see. Warning: lower levels than INFO might" +
            " leak sensitive information!");
    }

    @Override
    public void execute(String... args) throws CommandException {
        if (args.length < 2) {
            throw new CommandException("Please specify a LogLevel of "
                                           + getLevels() + ".");
        }

        Level level;
        try {
            level = Level.parse(args[1].toUpperCase(Locale.ENGLISH));
        } catch (Exception e) {
            throw new CommandException("Couldn't set level to '" + args[1]
                                           + "', please use one of "
                                           + getLevels() + ".");
        }

        LogLevelUtil.setLevel(level);
        for (Level l : getLevels()) {
            log.log(l, "Logging with Level: " + l.getName());
        }
    }

}
