package org.kar

import org.junit.Test
import org.kar.hudson.api.JSONApi
import org.kar.hudson.api.JobJSONApi
import org.kar.hudson.api.cli.HudsonCliApi
import org.kar.hudson.api.HudsonControlApi
import static java.net.HttpURLConnection.HTTP_OK
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP

class JenkinsApiTest
{
    @Test
    void testApi() {
        String rootUrl = 'http://localhost:8080/'
        JSONApi api = new JSONApi()
        JobJSONApi jobApi = new JobJSONApi()
        HudsonCliApi cliApi = new HudsonCliApi()
        HudsonControlApi controlApi = new HudsonControlApi()

/* Example usages */
//  Create a new job based on a config.xml file. Useful for copying a build from one node to another.
        assert HTTP_OK == jobApi.createJob(rootUrl, new File('../src/test/resources/config.xml').text, 'test2')

// And then delete that job
        def apiResult = api.inspectApi(rootUrl)
        def newJob = apiResult.jobs.find{it.name == 'test2'}
        assert HTTP_MOVED_TEMP == jobApi.deleteJob(newJob.url)

//  Execute scripts remotely. Output streams are printed to System.err and System.out,
// so println's on the server appear locally.
        def listPlugins = 'jenkins.model.Jenkins.instance.pluginManager.plugins.each { \
println("${it.longName} - ${it.version}") };'

        def allFailedBuilds = '''hudsonInstance = hudson.model.Hudson.instance
allItems = hudsonInstance.items
activeJobs = allItems.findAll{job -> job.isBuildable()}
failedRuns = activeJobs.findAll{job -> job.lastBuild && job.lastBuild.result == hudson.model.Result.FAILURE}
failedRuns.each{run -> println(run.name)}'''

        def parseableAllFailedBuilds = '''hudsonInstance = hudson.model.Hudson.instance
allItems = hudsonInstance.items
activeJobs = allItems.findAll{job -> job.isBuildable()}
failedRuns = activeJobs.findAll{job -> job.lastBuild && job.lastBuild.result == hudson.model.Result.FAILURE}
[activeJobs:activeJobs?.collect{it.name}, failedRuns:failedRuns?.collect{it.name}].inspect()'''

        [listPlugins, allFailedBuilds, parseableAllFailedBuilds].each{ script ->
            cliApi.runCliCommand(rootUrl, ['groovysh', script])
        }

// Execute a script remotely and capture the output for further study.
        OutputStream output = new ByteArrayOutputStream()
        cliApi.runCliCommand(rootUrl, ['groovysh', parseableAllFailedBuilds],System.in, output, System.err)
        def mapOfBuildsString = output.toString().substring(11) //remove some of the output characters that are not part of the returned value
        Map mapOfBuilds = Eval.me(mapOfBuildsString)  // convert to a map using the easiest available method
        assert mapOfBuilds.activeJobs
        assert mapOfBuilds.failedRuns

//  Install a new plugin. Easily automates configuring a new node with the required plugins.
//  This will also upgrade a plugin to the latest version if it is already installed.
        cliApi.runCliCommand(rootUrl, ['install-plugin', 'jira'])

//  Inform a node that it should get ready to shut down.
        println controlApi.sendQuiet(rootUrl)

//  Restart a node, required for newly installed plugins to be made available.
        cliApi.runCliCommand(rootUrl, 'safe-restart')
    }
}
