package org.jenkinsci.plugins;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import groovy.lang.GroovyShell;
import hudson.Extension;
import hudson.matrix.Axis;
import hudson.matrix.AxisDescriptor;
import hudson.matrix.MatrixBuild;
import hudson.util.FormValidation;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GroovyAxis extends Axis {

    private String groovyString;
    private List<String> computedValues;

    @DataBoundConstructor
    public GroovyAxis(String name, String groovyString, List<String> computedValues) {
        super(name, evaluateGroovy(groovyString));
        this.computedValues = computedValues;
        this.groovyString = groovyString;
    }

    public String getGroovyString() {
        return groovyString;
    }

    @Override
    public List<String> rebuild(MatrixBuild.MatrixBuildExecution context) {
        computedValues = evaluateGroovy(groovyString);
        return computedValues;
    }

    @Override
    public List<String> getValues() {
        return computedValues;
    }

    static private List<String> evaluateGroovy(String groovyExpression) {
        GroovyShell shell = new GroovyShell();
        Object result = shell.evaluate(groovyExpression);

        List<String> values = Lists.newArrayList();

        if (result instanceof ArrayList<?>) {
            ArrayList<?> objects = (ArrayList<?>) result;

            for (Object object : objects) {
                if (object instanceof String) {
                    values.add((String) object);
                }
            }
        } else if (result instanceof String) {
            values.add((String) result);
        }

        if (values.isEmpty()) {
            values.add("default");
        }

        return values;
    }

    @Extension
    public static class DescriptorImpl extends AxisDescriptor {

        @Override
        public String getDisplayName() {
            return "GroovyAxis";
        }

        @Override
        public Axis newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new GroovyAxis(
                    req.getParameter("name"),
                    req.getParameter("valueString"),
                    GroovyAxis.evaluateGroovy(req.getParameter("valueString"))
            );
        }

        public FormValidation doTestGroovyScript(
                StaplerRequest req,
                StaplerResponse rsp,
                @QueryParameter("valueString") final String valueString) throws IOException, ServletException {
            return FormValidation.ok(
                    new StringBuilder()
                            .append("[ ")
                            .append(Joiner.on(", ").join(GroovyAxis.evaluateGroovy(valueString)))
                            .append(" ]")
                            .toString()
            );
        }

    }

}
