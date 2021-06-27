package tech.metacontext.ocnhfa.composer.cf.ex;

import tech.metacontext.ocnhfa.composer.cf.model.MusicTrace;

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
/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class UnexpectedLocationException extends RuntimeException {

    /**
     * Creates a new instance of <code>UnexpectedLocation</code> without detail
     * message.
     */
    public UnexpectedLocationException() {
        
    }

    /**
     * Constructs an instance of <code>UnexpectedLocation</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public UnexpectedLocationException(String msg) {
        
        super(msg);
    }

    /**
     * Constructs an instance of <code>UnexpectedLocation</code> with the
     * specified <code>MusicTrace</code>.
     *
     * @param location current <code>MusicTrace</code> object.
     */
    public UnexpectedLocationException(MusicTrace location) {
        
        super(location.toString());
    }
}
