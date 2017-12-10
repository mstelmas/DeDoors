package org.wsd;

import io.vavr.control.Try;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.stream.Stream;

@Slf4j
public class GuiLookAndFeelManager {

    public Try<Void> setUpLookAndFeel(final String preferredLookAndFeel) {
        return loadLookAndFeelTheme(preferredLookAndFeel)
                .onFailure((e) -> log.warn("Could not load theme: {}, loading system default theme...", preferredLookAndFeel))
                .orElse(this::loadSystemLookAndFeel);
    }

    private Try<Void> loadLookAndFeelTheme(@NonNull final String lookAndFeelTheme) {
        return Try.run(() -> UIManager.setLookAndFeel(
                Stream.of(UIManager.getInstalledLookAndFeels())
                        .filter(lookAndFeelInfo -> lookAndFeelInfo.getName().equals(lookAndFeelTheme))
                        .findAny()
                        .map(UIManager.LookAndFeelInfo::getClassName)
                        .orElseThrow(Exception::new)
        ));
    }

    private Try<Void> loadSystemLookAndFeel() {
        return Try.run(() -> UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()));
    }

}
