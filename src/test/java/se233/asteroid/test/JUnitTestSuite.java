package se233.asteroid.test;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({AsteroidTests.class, BossTest.class,ScoringTest.class, PlayerShipActionTests.class,
        PlayerShipTest.class, RegularEnemyTests.class, SecondTierTest.class})
public class JUnitTestSuite {
    @BeforeAll
    public static void initJfxRuntime() {
        Platform.startup(() -> {});
    }
}