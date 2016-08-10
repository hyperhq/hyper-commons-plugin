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

package sh.hyper.plugins.hypercommons.tools;

import hudson.Extension;
import hudson.FilePath;
import hudson.ProxyConfiguration;
import hudson.model.Node;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import hudson.tools.ToolInstallation;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolInstallerDescriptor;
import hudson.util.IOUtils;
import jenkins.MasterToSlaveFileCallable;
import org.kohsuke.stapler.DataBoundConstructor;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author <a href="mailto:xlgao@zju.edu.cn">Xianglin Gao</a>
 */
public class HyperToolInstaller extends ToolInstaller {
    @DataBoundConstructor
    public HyperToolInstaller(String label) {
        super(label);
    }

    @Override
    public FilePath performInstallation(ToolInstallation toolInstallation, @Nonnull Node node, TaskListener listener) throws IOException, InterruptedException {

        VirtualChannel nodeChannel = node.getChannel();
        if (nodeChannel == null) {
            throw new IllegalStateException("Node is offline");
        }

        final URL url = new URL("https://hyper-install.s3.amazonaws.com/hyper-1.10-x86_64");

        FilePath install = preferredLocation(tool, node);

        // (simplified) copy/paste from FilePath as hudson.FilePath.installIfNecessaryFrom do assume the URL points to be a zip/tar archive

        FilePath timestamp = install.child(".timestamp");
        URLConnection con = null;
        long sourceTimestamp;
        try {
            con = ProxyConfiguration.open(url);
            if (timestamp.exists()) {
                con.setIfModifiedSince(timestamp.lastModified());
            }
            con.connect();
            if (con instanceof HttpURLConnection
                    && ((HttpURLConnection)con).getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                return install;
            }
            sourceTimestamp = con.getLastModified();

        } catch (IOException x) {
            if (install.exists()) {
                // Cannot connect now, so assume whatever was last unpacked is still OK.
                if (listener != null) {
                    listener.getLogger().println("Skipping installation: " + x);
                }
                return install;
            } else {
                throw x;
            }
        } finally {
            if (con instanceof HttpURLConnection) ((HttpURLConnection) con).disconnect();
        }

        if (install.exists()) {
            if (timestamp.exists() && sourceTimestamp == timestamp.lastModified())
                return install;   // already up to date
            install.deleteContents();
        }

        listener.getLogger().println("Downloading Hyper client.");
        FilePath bin = install.child("bin");
        bin.mkdirs();
        FilePath hyper = bin.child("hyper");

        if (install.isRemote()) {
            // First try to download from the slave machine.
            try {
                install.act(new MasterToSlaveFileCallable<Object>() {
                    public Object invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                        InputStream in = url.openStream();
                        IOUtils.copy(in, f);
                        in.close();
                        return null;
                    }
                });
            } catch (IOException x) {
                x.printStackTrace(listener.error("Failed to download " + url + " from slave; will retry from master"));
                InputStream in = ProxyConfiguration.getInputStream(url);
                hyper.copyFrom(in);
                in.close();
            }
        } else {
            InputStream in = ProxyConfiguration.getInputStream(url);
            hyper.copyFrom(in);
            in.close();
        }

        hyper.act(new MasterToSlaveFileCallable<Object>() {
            public Object invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                f.setExecutable(true);
                return null;
            }
        });

        timestamp.touch(sourceTimestamp);
        return install;
    }

    @Extension
    public static class DescriptorImpl extends ToolInstallerDescriptor<HyperToolInstaller> {

        @Override
        public String getDisplayName() {
            return "Install latest from hyper.sh";
        }

        @Override
        public boolean isApplicable(Class<? extends ToolInstallation> toolType) {
            return toolType == HyperTool.class;
        }
    }

}
