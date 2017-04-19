package quality.gates.sonar.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quality.gates.jenkins.plugin.GlobalConfigDataForSonarInstance;
import quality.gates.sonar.api5x.SonarHttpRequester5x;
import quality.gates.sonar.api60.SonarHttpRequester60;
import quality.gates.sonar.api61.SonarHttpRequester61;
import quality.gates.sonar.api63.SonarHttpRequester63;

/**
 * @author arkanjoms
 * @since 1.0
 */
public class SonarHttpRequesterFactory {

    private static Logger LOGGER = LoggerFactory.getLogger(SonarHttpRequesterFactory.class);

    private static final String SONAR_API_SERVER_VERSION = "/api/server/version";

    private String sonarVersion;

    public SonarHttpRequester getSonarHttpRequester(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {

        LOGGER.info("getSonarHttpRequester");

        getSonarApiServerVersion(globalConfigDataForSonarInstance);

        if (majorSonarVersion() <= 5) {
            LOGGER.info("SonarHttpRequester5x");
            return new SonarHttpRequester5x();
        } else if (minorSonarVersion() == 0) {
            LOGGER.info("SonarHttpRequester60");
            return new SonarHttpRequester60();
        } else if (minorSonarVersion() <= 2) {
            LOGGER.info("SonarHttpRequester61");
            return new SonarHttpRequester61();
        } else if (minorSonarVersion() == 3) {
            LOGGER.info("SonarHttpRequester63");
            return new SonarHttpRequester63();
        } else {
            LOGGER.info("UnsuportedVersionException");
            throw new UnsuportedVersionException("Plugin doesn't suport this version of sonar api! Please contact the developer.");
        }
    }

    private void getSonarApiServerVersion(GlobalConfigDataForSonarInstance globalConfigDataForSonarInstance) {

        sonarVersion = globalConfigDataForSonarInstance.getSonarUrl() + SONAR_API_SERVER_VERSION;
        LOGGER.info("getSonarApiServerVersion => " + sonarVersion);
    }

    private int majorSonarVersion() {

        return (int) sonarVersion.charAt(0);
    }

    private int minorSonarVersion() {

        return (int) sonarVersion.charAt(2);
    }
}
