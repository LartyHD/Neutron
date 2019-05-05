/*
* This file is part of Neutron, licensed under the MIT License
*
* Copyright (c) 2019 Crypnotic <crypnoticofficial@gmail.com>
*
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in all
* copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
* SOFTWARE.
*/
package me.crypnotic.neutron.module.announcement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.velocitypowered.api.scheduler.ScheduledTask;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.crypnotic.neutron.NeutronPlugin;
import net.kyori.text.TextComponent;

@RequiredArgsConstructor
public class Announcement {

    private final NeutronPlugin plugin;
    @Getter
    private final String id;
    @Getter
    private final AnnouncementData data;

    @Getter
    private ScheduledTask task;
    private List<TextComponent> localMessages;
    private volatile int index = 0;

    private void broadcast() {
        if (index == 0) {
            if (localMessages == null) {
                /* Create a local copy to avoid reading or shuffling the master copy */
                this.localMessages = new ArrayList<TextComponent>(data.getMessages());
            }

            if (!data.isMaintainOrder()) {
                Collections.shuffle(localMessages);
            }
        }

        plugin.getProxy().broadcast(data.getPrefix().append(localMessages.get(index)));

        index += 1;
        if (index == localMessages.size()) {
            index = 0;
        }
    }

    public static Announcement schedule(NeutronPlugin neutron, String id, AnnouncementData data) {
        Announcement announcement = new Announcement(neutron, id, data);

        announcement.task = neutron.getProxy().getScheduler().buildTask(neutron, announcement::broadcast)
                .repeat(announcement.data.getInterval(), TimeUnit.SECONDS).schedule();

        return announcement;
    }
}