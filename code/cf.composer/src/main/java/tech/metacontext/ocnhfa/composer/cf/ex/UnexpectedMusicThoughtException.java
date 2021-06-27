/*
 * Copyright 2021 Jonathan Chang, Chun-yien <ccy@musicapoetica.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tech.metacontext.ocnhfa.composer.cf.ex;

import tech.metacontext.ocnhfa.composer.cf.model.enums.MusicThought;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class UnexpectedMusicThoughtException extends RuntimeException {

    /**
     * Creates a new instance of <code>UnexpectedMusicThoughtException</code>
     * without detail message.
     */
    public UnexpectedMusicThoughtException() {

    }

    /**
     * Constructs an instance of <code>UnexpectedMusicThoughtException</code>
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public UnexpectedMusicThoughtException(String msg) {

        super(msg);
    }

    /**
     * Constructs an instance of <code>UnexpectedMusicThoughtException</code>
     * with the specified detail message.
     *
     * @param mt the unexpected MusicThought
     */
    public UnexpectedMusicThoughtException(MusicThought mt) {

        super("MusicThought = " + mt.name());
    }
}
