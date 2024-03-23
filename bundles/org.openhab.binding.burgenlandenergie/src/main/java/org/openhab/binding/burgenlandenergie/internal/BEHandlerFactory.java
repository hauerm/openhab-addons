/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.burgenlandenergie.internal;

import static org.openhab.binding.burgenlandenergie.internal.BEBindingConstants.*;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.burgenlandenergie.internal.api.HttpClientSingleton;
import org.openhab.binding.burgenlandenergie.internal.handler.BEBridgeHandler;
import org.openhab.binding.burgenlandenergie.internal.handler.ElectricityTariffThingHandler;
import org.openhab.binding.burgenlandenergie.internal.handler.NaturalGasTariffThingHandler;
import org.openhab.binding.burgenlandenergie.internal.utils.GsonSingleton;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link BEHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Michael Hauer - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.burgenlandenergie", service = ThingHandlerFactory.class)
public class BEHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_BRIDGE, THING_ELECTRICITY_TARIFF,
            THING_NATURALGAS_TARIFF);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (THING_BRIDGE.equals(thingTypeUID)) {
            return new BEBridgeHandler((Bridge) thing);
        } else if (THING_ELECTRICITY_TARIFF.equals(thingTypeUID)) {
            return new ElectricityTariffThingHandler(thing);
        } else if (THING_NATURALGAS_TARIFF.equals(thingTypeUID)) {
            return new NaturalGasTariffThingHandler(thing);
        }

        return null;
    }

    @Override
    protected void deactivate(ComponentContext componentContext) {
        super.deactivate(componentContext);
        HttpClientSingleton.destroy();
        GsonSingleton.destroy();
    }
}
