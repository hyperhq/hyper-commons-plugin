/*
 * The MIT License
 *
 *  Copyright (c) 2016 HyperHQ Inc
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  THE SOFTWARE.
 *
 */

package sh.hyper.plugins.hypercommons;

import hudson.Extension;
import hudson.Plugin;
import hudson.model.Describable;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:xlgao@zju.edu.cn">Xianglin Gao</a>
 */

public class Tools extends Plugin implements Describable<Tools> {
    @Override
    public Descriptor<Tools> getDescriptor() {
        return Jenkins.getInstance().getDescriptor(getClass());
    }

    @Extension
    public static final class DescriptorImpl extends Descriptor<Tools> {
        private String accessId;
        private String secretKey;

        public DescriptorImpl() {
            load();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            accessId = formData.getString("accessId");
            secretKey = formData.getString("secretKey");

            save();

            return super.configure(req, formData);
        }

        public String getAccessId() {
            return accessId;
        }

        public String getSecretKey() {
            return secretKey;
        }

        @Override
        public String getDisplayName() {
            return "Hyper common plugin";
        }

        //save credential
        public FormValidation doSaveCredential(@QueryParameter("accessId") final String accessId,
                                               @QueryParameter("secretKey") final String secretKey) throws IOException, ServletException {
            try {
                String jsonStr = "{\"clouds\": {" +
                        "\"tcp://us-west-1.hyper.sh:443\": {" +
                        "\"accesskey\": " + "\"" + accessId + "\"," +
                        "\"secretkey\": " + "\"" + secretKey + "\"" +
                        "}" +
                        "}" +
                        "}";
                OutputStreamWriter jsonWriter = null;
                String configPath;
                File jenkinsHome = Jenkins.getInstance().getRootDir();

                File hyperPath = new File(jenkinsHome.getPath() + "/.hyper");
                try {
                    if (!hyperPath.exists()) {
                        if (!hyperPath.mkdir()) return FormValidation.ok("Saving credentials failed!");
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                configPath = jenkinsHome.getPath() + "/.hyper/config.json";

                File config = new File(configPath);
                if (!config.exists()) {
                    try {
                        if (!config.createNewFile()) return FormValidation.ok("Saving credentials failed!");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    FileOutputStream configFile = new FileOutputStream(configPath);
                    jsonWriter = new OutputStreamWriter(configFile, "UTF-8");
                    jsonWriter.write(jsonStr);
                    jsonWriter.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (jsonWriter != null) {
                            jsonWriter.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return FormValidation.ok("Credentials saved!");
            } catch (Exception e) {
                return FormValidation.error("Saving credentials error : " + e.getMessage());
            }
        }

        public FormValidation doTestConnection() throws IOException, ServletException {
            try {
                Process hypercli = null;
                try {
                    String command = "hyper info";
                    Runtime runtime = Runtime.getRuntime();
                    hypercli = runtime.exec(command);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (hypercli == null) {
                    return FormValidation.ok("connection test failed!");
                } else {
                    hypercli.waitFor(10, TimeUnit.SECONDS);
                }

                if (hypercli.exitValue() == 0) {
                    return FormValidation.ok("connection test succeeded!");
                } else {
                    return FormValidation.ok("connection test failed!");
                }
            } catch (Exception e) {
                return FormValidation.error("connection test error : " + e.getMessage());
            }
        }

        //download Hypercli
        public FormValidation doDownloadHypercli() throws IOException, ServletException {
            try {
                String urlPath = "https://mirror-hyper-install.s3.amazonaws.com/hyper";
                String hyperCliPath;
                URL url = new URL(urlPath);
                URLConnection connection = url.openConnection();
                InputStream in = connection.getInputStream();
                FileOutputStream os = null;

                File jenkinsHome = Jenkins.getInstance().getRootDir();

                File hyperPath = new File(jenkinsHome.getPath() + "/bin");
                try {
                    if (!hyperPath.exists()) {
                        if (!hyperPath.mkdir()) return FormValidation.ok("downloading hypercli failed!");
                    }
                } catch (SecurityException e) {
                    e.printStackTrace();
                }
                hyperCliPath = jenkinsHome.getPath() + "/bin/hyper";

                try{
                    os = new FileOutputStream(hyperCliPath);
                    byte[] buffer = new byte[4 * 1024];
                    int read;
                    while ((read = in.read(buffer)) > 0) {
                        os.write(buffer, 0, read);
                    }
                    os.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (os != null) {
                            os.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                in.close();

                try {
                    String command = "chmod +x " + hyperCliPath;
                    Runtime runtime = Runtime.getRuntime();
                    runtime.exec(command);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return FormValidation.ok("Hypercli downloaded!");
            } catch (Exception e) {
                return FormValidation.error("Downloading Hypercli error : " + e.getMessage());
            }
        }
    }
}
