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
package tech.metacontext.ocnhfa.composer.cf.model.y;

import java.util.Objects;
import tech.metacontext.ocnhfa.antsomg.impl.StandardVertex;
import tech.metacontext.ocnhfa.composer.cf.model.enums.Pitch;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class PitchNode extends StandardVertex {

    private Pitch pitch;

    public PitchNode(Pitch pitch) {

        super(pitch.name());
        this.pitch = pitch;
    }

    public Pitch getPitch() {

        return pitch;
    }

    public void setPitch(Pitch pitch) {

        this.pitch = pitch;
    }

    @Override
    public int hashCode() {
      
        int hash = 5;
        hash = 73 * hash + Objects.hashCode(this.pitch);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
      
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final PitchNode other = (PitchNode) obj;
        return this.pitch == other.pitch;
    }
}
