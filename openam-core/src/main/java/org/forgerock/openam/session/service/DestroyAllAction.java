/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011-2015 ForgeRock AS. All Rights Reserved
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://forgerock.org/license/CDDLv1.0.html
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at http://forgerock.org/license/CDDLv1.0.html
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 */
package org.forgerock.openam.session.service;

import static org.forgerock.openam.session.SessionConstants.*;

import com.google.inject.Key;
import com.google.inject.name.Names;
import com.iplanet.dpro.session.Session;
import com.iplanet.dpro.session.SessionException;
import com.iplanet.dpro.session.SessionID;
import com.iplanet.dpro.session.service.InternalSession;
import com.iplanet.dpro.session.service.QuotaExhaustionAction;
import com.sun.identity.shared.debug.Debug;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.forgerock.guice.core.InjectorHolder;
import org.forgerock.openam.session.SessionCache;

/**
 * This action will invalidate all currently existing sessions, but it will
 * let the current session to get created, so this way the user will always have
 * only one session.
 *
 * @author Steve Ferris
 */
public class DestroyAllAction implements QuotaExhaustionAction {

    private static Debug debug = InjectorHolder.getInstance(Key.get(Debug.class, Names.named(SESSION_DEBUG)));

    private final SessionCache sessionCache;

    public DestroyAllAction() {
        this.sessionCache = InjectorHolder.getInstance(SessionCache.class);
    }

    @Inject
    public DestroyAllAction(SessionCache sessionCache) {
        this.sessionCache = sessionCache;
    }

    @Override
    public boolean action(InternalSession is, Map sessions) {
        final Set<String> sids = sessions.keySet();
        debug.message("there are " + sids.size() + " sessions");
        for (String sid : sids) 
        		if (!StringUtils.equals(is.getSessionID().toString(), sid))
	        {
	            final SessionID sessID = new SessionID(sid);
	            try {
	                Session s=sessionCache.getSession(sessID, true, false);
	                s.destroySession(s);
	                debug.error("{} {} {} {}", this.getClass().getSimpleName(), sessID,is.getClientID(),s.getIdleTime());
	                s.destroySession(s);
	            } catch (SessionException se) {
	                debug.error("{} {} {} {} {}", this.getClass().getSimpleName(), sessID,is.getClientID(),sessions.size()+1,se.toString());
	            }finally {
	            		sessions.remove(sid);
				}
	        }
        return false;
    }
}
