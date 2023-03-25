/* Copyright 2018-2022 Norconex Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.norconex.commons.lang.net;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Proxy;

import org.apache.commons.lang3.StringUtils;

import com.norconex.commons.lang.bean.BeanUtil;
import com.norconex.commons.lang.security.Credentials;
import com.norconex.commons.lang.xml.XML;
import com.norconex.commons.lang.xml.XMLConfigurable;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * Convenience class for implementation requiring proxy settings.
 * </p>
 *
 * {@nx.include com.norconex.commons.lang.security.Credentials#doc}
 *
 * {@nx.xml.usage
 * <host>
 *   {@nx.include com.norconex.commons.lang.net.Host@nx.xml.usage}
 * </host>
 * <scheme>(Default is "http")</scheme>
 * <realm>(Authentication realm. Default is any.)</realm>
 * <credentials>
 *   {@nx.include com.norconex.commons.lang.security.Credentials@nx.xml.usage}
 * </credentials>
 * }
 * <p>The above can be found under any parent tag. See consuming class
 * documentation for exact usage.</p>
 *
 * @since 1.14.0
 */
@SuppressWarnings("javadoc")
@ToString
@EqualsAndHashCode
@FieldNameConstants(level = AccessLevel.PRIVATE)
@Slf4j
public class ProxySettings implements XMLConfigurable, Serializable {

    private static final long serialVersionUID = 1L;

    private Host host;
    private String scheme;
    private final Credentials credentials = new Credentials();
    private String realm;

    public ProxySettings() {}
    public ProxySettings(String name, int port) {
        this(new Host(name, port));
    }
    public ProxySettings(Host host) {
        this.host = host;
    }

    /**
     * Gets the proxy host.
     * @return proxy host or <code>null</code> if not set
     * @see #isSet()
     */
    public Host getHost() {
        return host;
    }
    /**
     * Sets the proxy host.
     * @param host proxy host
     * @return this
     */
    public ProxySettings setHost(Host host) {
        this.host = host;
        return this;
    }

    /**
     * Gets the proxy scheme.
     * @return proxy scheme
     * @since 2.0.0
     */
    public String getScheme() {
        return scheme;
    }
    /**
     * Sets the proxy scheme.
     * @param scheme proxy scheme
     * @return this
     * @since 2.0.0
     */
    public ProxySettings setScheme(String scheme) {
        this.scheme = scheme;
        return this;
    }

    /**
     * Gets the proxy credentials.
     * @return proxy credentials (never <code>null</code>)
     * @since 2.0.0
     */
    public Credentials getCredentials() {
        return credentials;
    }
    /**
     * Sets the proxy credentials.
     * @param credentials proxy credentials
     * @return this
     * @since 2.0.0
     */
    public ProxySettings setCredentials(Credentials credentials) {
        this.credentials.copyFrom(credentials);
        return this;
    }

    /**
     * Gets the proxy realm.
     * @return proxy realm
     * @since 2.0.0
     */
    public String getRealm() {
        return realm;
    }
    /**
     * Sets the proxy realm.
     * @param realm proxy realm
     * @return this
     * @since 2.0.0
     */
    public ProxySettings setRealm(String realm) {
        this.realm = realm;
        return this;
    }

    public boolean isSet() {
        return host != null && host.isSet();
    }

    public void copyTo(ProxySettings another) {
        BeanUtil.copyProperties(another, this);
    }
    public void copyFrom(ProxySettings another) {
        BeanUtil.copyProperties(this, another);
    }

    /**
     * Converts this proxy settings to a {@link Proxy}. The scheme is used
     * to establish the proxy type.  If {@link #isSet()} returns false,
     * {@link Proxy#NO_PROXY} is returned.
     * @return proxy
     * @since 3.0.0
     */
    public Proxy toProxy() {
        if (!isSet()) {
            return Proxy.NO_PROXY;
        }
        var type = Proxy.Type.HTTP;
        if (StringUtils.startsWithIgnoreCase(scheme, "socks")) {
            type = Proxy.Type.SOCKS;
        } else if (StringUtils.isNotBlank(scheme)
                && !StringUtils.startsWithIgnoreCase(scheme, "http")) {
            LOG.warn("Unsupported proxy scheme: '{}'. Defaulting to HTTP.",
                    scheme);
        }
        var address = new InetSocketAddress(host.getName(), host.getPort());
        return new Proxy(type, address);
    }

    @Override
    public void loadFromXML(XML xml) {
        if (xml != null) {
            xml.checkDeprecated("proxyHost", Fields.host, true);
            xml.checkDeprecated("proxyPort", Fields.host, true);
            xml.checkDeprecated("proxyScheme", Fields.scheme, true);
            xml.checkDeprecated("proxyRealm", Fields.realm, true);
            //MAYBE: make host XMLConfigurable and use populate() ?
            host = Host.loadFromXML(xml.getXML(Fields.host), host);
            scheme = xml.getString(Fields.scheme, scheme);
            realm = xml.getString(Fields.realm, realm);
            xml.ifXML(Fields.credentials, x -> x.populate(credentials));
        }
    }

    @Override
    public void saveToXML(XML xml) {
        if (xml != null) {
            Host.saveToXML(xml.addElement(Fields.host), host);
            xml.addElement(Fields.scheme, scheme);
            xml.addElement(Fields.realm, realm);
            credentials.saveToXML(xml.addElement(Fields.credentials));
        }
    }
}
