/*
 *   Copyright (c) 2007, 2008 Florian MAZEN
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package f4fez.prm80x0.Controler;

import java.util.ArrayList;

/**
 *
 * @author fmazen
 */
public class ChannelList{
    private ArrayList<Channel> list;

    public ChannelList() {
        this.list = new ArrayList();
    }

    public Channel getChannel(int channel) {
        return this.list.get(channel);
    }

    public void addChannel(Channel channel) {
        this.list.add(channel);
        channel.setId(this.list.indexOf(channel));
    }

    public int countChannel() {
        return this.list.size();
    }

}
