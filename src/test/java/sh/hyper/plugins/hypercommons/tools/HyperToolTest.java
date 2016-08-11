/*
 * The MIT License
 *
 * Copyright 2015 Jesse Glick.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package sh.hyper.plugins.hypercommons.tools;

import hudson.slaves.DumbSlave;
import hudson.tools.ToolLocationNodeProperty;
import hudson.tools.ToolProperty;
import hudson.util.StreamTaskListener;
import java.util.Collections;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.jvnet.hudson.test.JenkinsRule;

public class HyperToolTest {

    @Rule public JenkinsRule r = new JenkinsRule();

    @Test public void getExecutable() throws Exception {
        assertEquals(HyperTool.COMMAND, HyperTool.getExecutable(null, null, null, null));
        HyperTool.DescriptorImpl descriptor = r.jenkins.getDescriptorByType(HyperTool.DescriptorImpl.class);
        String name = "hyper15";
        descriptor.setInstallations(new HyperTool(name, "/usr/local/hyper15", Collections.<ToolProperty<?>>emptyList()));
        // TODO r.jenkins.restart() does not reproduce need for get/setInstallations; use RestartableJenkinsRule in 1.567+
        assertEquals("/usr/local/hyper15/bin/hyper", HyperTool.getExecutable(name, null, null, null));
        DumbSlave slave = r.createOnlineSlave();
        slave.getNodeProperties().add(new ToolLocationNodeProperty(new ToolLocationNodeProperty.ToolLocation(descriptor, name, "/opt/hyper")));
        assertEquals("/usr/local/hyper15/bin/hyper", HyperTool.getExecutable(name, null, null, null));
        assertEquals("/opt/hyper/bin/hyper", HyperTool.getExecutable(name, slave, StreamTaskListener.fromStderr(), null));
    }

}
