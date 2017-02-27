/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.persist;

import java.util.Objects;

import org.opennms.netmgt.model.foreignsource.DetectorPluginConfigEntity;
import org.opennms.netmgt.model.foreignsource.ForeignSourceEntity;
import org.opennms.netmgt.model.foreignsource.PolicyPluginConfigEntity;

public class LegacyForeignSourceServiceImpl extends DefaultForeignSourceService implements LegacyForeignSourceService {

    @Override
    public ForeignSourceEntity saveForeignSource(String name, ForeignSourceEntity fs) {
        saveForeignSource(fs);
        return getForeignSource(fs.getName());
    }

    @Override
    public ForeignSourceEntity cloneForeignSource(String name, String target) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(target);
        if (name.equals(target)) {
            throw new IllegalArgumentException("name and target must not be identical");
        }
        if (getForeignSource(target) != null) {
            throw new IllegalArgumentException("ForeignSource with name '" + target + "' already exists. Cannot clone");
        }
        // TODO MVR clone (this does not clone..)
        final ForeignSourceEntity foreignSource = getForeignSource(name);
        foreignSource.setDefault(false);
        foreignSource.setName(target);
        saveForeignSource(foreignSource);
        return getForeignSource(target);
    }

    @Override
    public ForeignSourceEntity addParameter(String foreignSourceName, String pathToAdd) {

//        ForeignSourceEntity fs = getForeignSource(foreignSourceName);
//        PropertyPath path = new PropertyPath(pathToAdd);
//        Object obj = path.getValue(fs);
//
//        try {
//            MethodUtils.invokeMethod(obj, "addParameter", new Object[] { "key", "value" });
//        } catch (NoSuchMethodException e) {
//            throw new IllegalArgumentException("Unable to call addParameter on object of type " + obj.getClass(), e);
//        } catch (IllegalAccessException e) {
//            throw new IllegalArgumentException("unable to access property "+pathToAdd, e);
//        } catch (InvocationTargetException e) {
//            throw new IllegalArgumentException("an execption occurred adding a parameter to "+pathToAdd, e);
//        }
//
//        m_pendingForeignSourceRepository.save(fs);
//        return fs;
        // TODO MVR implement me
        return null;
    }

    @Override
    public ForeignSourceEntity deletePath(String foreignSourceName, String pathToDelete) {
//        ForeignSource fs = getForeignSource(foreignSourceName);
//        PropertyPath path = new PropertyPath(pathToDelete);
//
//        Object objToDelete = path.getValue(fs);
//        Object parentObject = path.getParent() == null ? fs : path.getParent().getValue(fs);
//
//        String propName = path.getPropertyName();
//        String methodSuffix = Character.toUpperCase(propName.charAt(0))+propName.substring(1);
//        String methodName = "delete"+methodSuffix;
//
//        try {
//            MethodUtils.invokeMethod(parentObject, methodName, new Object[] { objToDelete });
//        } catch (NoSuchMethodException e) {
//            throw new IllegalArgumentException("Unable to find method "+methodName+" on object of type "+parentObject.getClass()+" with argument " + objToDelete, e);
//        } catch (IllegalAccessException e) {
//            throw new IllegalArgumentException("unable to access property "+pathToDelete, e);
//        } catch (InvocationTargetException e) {
//            throw new IllegalArgumentException("an execption occurred deleting "+pathToDelete, e);
//        }
//
//        m_pendingForeignSourceRepository.save(fs);
//        return fs;
        // TODO MVR implement me
        return null;
    }

    @Override
    public ForeignSourceEntity addDetectorToForeignSource(String foreignSource, String name) {
        ForeignSourceEntity fs = getForeignSource(foreignSource);
        DetectorPluginConfigEntity pc = new DetectorPluginConfigEntity(name, "unknown");
        fs.addDetector(pc);
        saveForeignSource(fs);
        return fs;
    }

    @Override
    public ForeignSourceEntity deleteDetector(String foreignSource, String name) {
        ForeignSourceEntity fs = getForeignSource(foreignSource);
        fs.removeDetector(name);
        saveForeignSource(fs);
        return fs;
    }

    @Override
    public ForeignSourceEntity addPolicyToForeignSource(String foreignSource, String name) {
        ForeignSourceEntity fs = getForeignSource(foreignSource);
        PolicyPluginConfigEntity pc = new PolicyPluginConfigEntity(name, "unknown");
        fs.addPolicy(pc);
        saveForeignSource(fs);
        return fs;
    }

    @Override
    public ForeignSourceEntity deletePolicy(String foreignSource, String name) {
        ForeignSourceEntity fs = getForeignSource(foreignSource);
        fs.removePolicy(name);
        saveForeignSource(fs);
        return fs;
    }
}
