package org.kar.hudson.api

import static org.kar.hudson.api.HudsonApiPaths.*

/**
 * @author Kelly Robinson
 */
class JSONApi
{
    private final GetRequestSupport requestSupport = new GetRequestSupport()

    /**
     * Display the json api for a particular Hudson url.
     * @param rootUrl the url to query for the hosted json api
     * @return  contents of api in json format
     */
    def inspectApi(String rootUrl, int depth = 0)
    {
        requestSupport.get(rootUrl, API_JSON, depth)
    }

    /**
     * Display the json api for a particular Hudson computer.
     * @param rootUrl the url of the Hudson server
     * @return  contents of computer api in json format
     */
    def inspectComputer(String rootUrl, int depth = 0)
    {
        requestSupport.get(rootUrl, COMPUTER, depth)
    }

    /**
     * Trigger an update check for plugin content.
     * NOTE: Presently does not work due to existing bugs with Jenkins
     * https://issues.jenkins-ci.org/browse/JENKINS-10061
     * https://issues.jenkins-ci.org/browse/JENKINS-10062
     * @param rootUrl
     * @param depth
     * @return
     */
    def checkForUpdates(String rootUrl, int depth = 0)
    {
        requestSupport.get(rootUrl, CHECK_UPDATES, depth)
    }
}
