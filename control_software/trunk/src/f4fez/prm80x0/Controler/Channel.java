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

/**
 *
 * @author fmazen
 */
public class Channel {

    private int id;

    public Channel() {
        this.id = -1;
    }

    /**
     * Get the value of id
     *
     * @return the value of id
     */
    public int getId() {
        return id;
    }

    /**
     * Set the value of id
     *
     * @param id new value of id
     */
    public void setId(int id) {
        this.id = id;
    }

    private String frequency;

    /**
     * Get the value of frequency
     *
     * @return the value of frequency
     */
    public String getFrequency() {
        return frequency;
    }

    /**
     * Set the value of frequency
     *
     * @param frequency new value of frequency
     */
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
    protected boolean shift;

    /**
     * Get the value of shift
     *
     * @return the value of shift
     */
    public boolean isShift() {
        return shift;
    }

    /**
     * Set the value of shift
     *
     * @param shift new value of shift
     */
    public void setShift(boolean shift) {
        this.shift = shift;
    }
    private String comments;

    /**
     * Get the value of comments
     *
     * @return the value of comments
     */
    public String getComments() {
        return comments;
    }

    /**
     * Set the value of comments
     *
     * @param comments new value of comments
     */
    public void setComments(String comments) {
        this.comments = comments;
    }

}
