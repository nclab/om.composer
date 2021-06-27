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
package tech.metacontext.ocnhfa.composer.cf.utils.io.musicxml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchNode;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
@Deprecated
public class Score {

    public static final String software = "MuseScore 3.2.3";
    private final String divisions = "1";
    private final String instrument_name = "Church Organ";
    private final String midi_channel = "1";
    private final String midi_program = "20";
    private final String part_id = "P1";
    private final String instrument_id = "P1-I1";
    private final String part_name = "cf";

    public String work_title;
    public final Element root;
    public final Element part;
    public final List<Element> measures;

    private int tempo;
    private boolean isLongerEnding;
    private final int unit_duration = 4;

    /**
     * Initiate a new Score for output as musicxml.
     *
     * @param work_title Text to be shown as Title.
     * @param source Text to be shown as Composer.
     */
    public Score(String work_title, String source) {

        var doc = DocumentHelper.createDocument()
                .addDocType("score-partwise",
                        "-//Recordare//DTD MusicXML 3.1 Partwise//EN",
                        "http://www.musicxml.org/dtds/partwise.dtd");
        doc.setXMLEncoding("UTF-8");
        this.root = doc.addElement("score-partwise")
                .addAttribute("version", "3.1");
        this.work_title = work_title;
        this.root.addElement("work")
                .addElement("work-title").addText(work_title);
        var identification = this.root.addElement("identification");
        if (Objects.nonNull(source)) {
            identification
                    .addElement("creator").addText(source)
                    .addAttribute("type", "composer");
        }
        identification
                .addElement("encoding")
                .addElement("software").addText(software);

        var part_list = root.addElement("part-list");
        {
            var score_part = part_list.addElement("score-part")
                    .addAttribute("id", part_id);
            score_part.addElement("part-name").addText(part_name);
            var score_instrument = score_part.addElement("score-instrument")
                    .addAttribute("id", instrument_id);
            score_instrument.addElement("instrument-name").addText(instrument_name);

            var midi_instrument = score_part.addElement("midi-instrument")
                    .addAttribute("id", instrument_id);
            midi_instrument.addElement("midi-channel").addText(midi_channel);
            midi_instrument.addElement("midi-program").addText(midi_program);
        }
        this.part = root.addElement("part")
                .addAttribute("id", part_id);
        this.measures = new ArrayList<>();
    }

    public Element addMeasure(Clef selected_clef, int number, List<PitchNode> melody) {

        var measure = part.addElement("measure")
                .addAttribute("number", String.valueOf(number));
        if (number > 1) {
            measure.addElement("print")
                    .addAttribute("new-system", "yes");
        }
        var attributes = measure.addElement("attributes");
        {
            attributes.addElement("divisions").setText(divisions);
            attributes.addElement("key")
                    .addElement("fifths").addText("0");
            var clef = attributes.addElement("clef");
            clef.addElement("sign").addText(selected_clef.sign);
            clef.addElement("line").addText(selected_clef.line);
        }
        if (tempo > 0) {
            measure.addElement("sound").addAttribute("tempo", String.valueOf(this.tempo));
        }
        if (isLongerEnding) {
            var rest = measure.addElement("note");
            rest.addElement("rest");
            rest.addElement("duration").setText(String.valueOf(unit_duration * 2));
            rest.addElement("type").setText("breve");
        }
        melody.forEach(pitch -> addNote(measure, pitch));
        if (isLongerEnding) {
            var last_note = measure.elements("note").get(melody.size());
            last_note.element("duration").setText(String.valueOf(unit_duration * 4));
            last_note.element("type").setText("long");
            var rest = measure.addElement("note");
            rest.addElement("rest");
            rest.addElement("duration").setText(String.valueOf(unit_duration * 2));
            rest.addElement("type").setText("breve");
        }
        measure.addElement("barline")
                .addAttribute("location", "right")
                .addElement("bar-style").addText("light-light");
        this.measures.add(measure);
        return measure;
    }

    public void addNote(Element measure, PitchNode node_pitch) {

        String step = String.valueOf(node_pitch.getName().charAt(0));
        int octave = Integer.valueOf(String.valueOf(node_pitch.getName().charAt(1)));
        var note = measure.addElement("note");
        var pitch = note.addElement("pitch");
        pitch.addElement("step").addText(step);
        pitch.addElement("octave").addText(String.valueOf(octave));
        note.addElement("duration").addText(String.valueOf(unit_duration));
        note.addElement("type").addText("whole");
    }

    public void saveScore(File score_path) {

        try ( var fw = new FileWriter(score_path);
              var bw = new BufferedWriter(fw);) {
            bw.write(this.toString());
        } catch (IOException ex) {
            Logger.getLogger(Score.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public String toString() {

        StringWriter sw = new StringWriter();
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter xw = new XMLWriter(sw, format);
        try {
            xw.write(this.root.getDocument());
        } catch (IOException ex) {
            Logger.getLogger(Score.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sw.toString();
    }

    public void setTempo(int tempo) {

        this.tempo = tempo;
    }

    public void setLongerEnding(boolean isLongerEnding) {

        this.isLongerEnding = isLongerEnding;
    }
}
