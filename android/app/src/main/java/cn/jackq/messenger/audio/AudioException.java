/*
 * Copyright (C) 2014 Andrew Comminos
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package cn.jackq.messenger.audio;

/**
 * Created on: 28/04/14.
 * Creator: Jack Q <qiaobo@outlook.com>
 */
public class AudioException extends Exception {
    public AudioException(String message) {
        super(message);
    }

    public AudioException(Throwable throwable) {
        super(throwable);
    }

    public AudioException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
