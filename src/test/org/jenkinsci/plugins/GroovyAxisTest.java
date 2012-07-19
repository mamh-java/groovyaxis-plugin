package org.jenkinsci.plugins;

import com.google.common.collect.Lists;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.tasks.Shell;
import org.jvnet.hudson.test.HudsonTestCase;

public class GroovyAxisTest extends HudsonTestCase {

    private static final String NOT_A_TIMESTAMP = "not a timestamp";

    public void testGroovyAxis() throws Exception {
        MatrixProject p = createMatrixProject();

        // The axis is initialized with fake computedValues
        p.getAxes().add(
                new GroovyAxis(
                        "myAxis",
                        "return new Date().getDateTimeString()",
                        Lists.newArrayList(NOT_A_TIMESTAMP)));
        p.getBuildersList().add(new Shell("echo $myAxis"));

        // The build is run. This should call rebuild() which changes computedValues
        MatrixBuild b = p.scheduleBuild2(0).get();

        assertLogNotContains(NOT_A_TIMESTAMP, b.getRuns().get(0));
    }
}
