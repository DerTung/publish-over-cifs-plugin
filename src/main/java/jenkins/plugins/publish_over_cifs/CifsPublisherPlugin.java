/*
 * The MIT License
 *
 * Copyright (C) 2010-2011 by Anthony Robinson
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

package jenkins.plugins.publish_over_cifs;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.Hudson;
import hudson.model.Node;
import jenkins.plugins.publish_over.BPBuildInfo;
import jenkins.plugins.publish_over.BPPlugin;
import jenkins.plugins.publish_over.BPPluginDescriptor;
import jenkins.plugins.publish_over_cifs.descriptor.CifsPublisherPluginDescriptor;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.ArrayList;

@SuppressWarnings("PMD.LooseCoupling") // serializable
public class CifsPublisherPlugin extends BPPlugin<CifsPublisher, CifsClient, Object> {

    private static final long serialVersionUID = 1L;

    @DataBoundConstructor
    public CifsPublisherPlugin(final ArrayList<CifsPublisher> publishers, final boolean continueOnError, final boolean failOnError,
                               final boolean alwaysPublishFromMaster, final String masterNodeName, final CifsParamPublish paramPublish) {
        super(Messages.console_message_prefix(), publishers, continueOnError, failOnError, alwaysPublishFromMaster, masterNodeName,
                paramPublish);
    }

    @Override
    protected void fixup(final AbstractBuild<?, ?> build, final BPBuildInfo buildInfo) {
        final Hudson hudson = Hudson.getInstance();
        final CifsNodeProperties defaults = hudson.getGlobalNodeProperties().get(CifsNodeProperties.class);
        if (defaults != null) buildInfo.put(CifsPublisher.CTX_KEY_NODE_PROPERTIES_DEFAULT, map(defaults));
        final String currNodeName = buildInfo.getCurrentBuildEnv().getEnvVars().get(BPBuildInfo.ENV_NODE_NAME);
        storeProperties(buildInfo, hudson, currNodeName, CifsPublisher.CTX_KEY_NODE_PROPERTIES_CURRENT);
    }

    private void storeProperties(final BPBuildInfo buildInfo, final Hudson hudson, final String nodeName, final String contextKey) {
        if (Util.fixEmptyAndTrim(nodeName) == null) return;
        final Node node = hudson.getNode(nodeName);
        if (node == null) return;
        final CifsNodeProperties currNodeProps = node.getNodeProperties().get(CifsNodeProperties.class);
        if (currNodeProps != null) buildInfo.put(contextKey, map(currNodeProps));
    }

    private CifsCleanNodeProperties map(final CifsNodeProperties nodeProperties) {
        if (nodeProperties == null) return null;
        return new CifsCleanNodeProperties(Util.fixEmptyAndTrim(nodeProperties.getWinsServer()));
    }

    public boolean equals(final Object that) {
        if (this == that) return true;
        if (that == null || getClass() != that.getClass()) return false;

        return addToEquals(new EqualsBuilder(), (CifsPublisherPlugin) that).isEquals();
    }

    public int hashCode() {
        return addToHashCode(new HashCodeBuilder()).toHashCode();
    }

    public String toString() {
        return addToToString(new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)).toString();
    }

    public Descriptor getDescriptor() {
        return Hudson.getInstance().getDescriptorByType(Descriptor.class);
    }

    public CifsHostConfiguration getConfiguration(final String name) {
        return getDescriptor().getConfiguration(name);
    }

    @Extension
    public static class Descriptor extends CifsPublisherPluginDescriptor {

        // While this looks redundant, it resolves some issues with XStream Reflection causing it
        // not to persist settings after a reboot
        @Override
        public Object readResolve() {
            return super.readResolve();
        }

    }

    public static class DescriptorMessages implements BPPluginDescriptor.BPDescriptorMessages { }

}
