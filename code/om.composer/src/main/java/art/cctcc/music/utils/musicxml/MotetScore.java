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
package art.cctcc.music.utils.musicxml;

import art.cctcc.music.cpt.graphs.y_cpt.CptPitchNode;
import art.cctcc.music.cpt.model.CptCounterpoint;
import art.cctcc.music.cpt.model.CptMelody;
import art.cctcc.music.motet.model.enums.SectionType;
import static art.cctcc.music.motet.model.enums.SectionType.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.String;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.util.Marshalling;
import org.audiveris.proxymusic.util.Marshalling.MarshallingException;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class MotetScore {

  private static final ObjectFactory OF = new ObjectFactory();

  private final ScorePartwise score_partwise;
  private final PartList part_list;

  private static final TimeSymbol TIME_SYMBOL = TimeSymbol.CUT;
  private static final int DIVISIONS = 1;
  private static final int DURATION = 4;
  private static final String INSTRUMENT_NAME = "Church Organ";
  private static final int MIDI_PROGRAM = 20;

  private static final String BEAT_UNIT = "whole";
  private static final Integer PER_MINUTE = 105;

  static {
    try {
      Marshalling.getContext(ScorePartwise.class);
    } catch (JAXBException ex) {
      Logger.getLogger(MotetScore.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
  }

  public MotetScore(String title, String composer,
          List<Map.Entry<SectionType, CptMelody>> composition) {

    score_partwise = OF.createScorePartwise();

    var work = OF.createWork();
    work.setWorkTitle(title);
    score_partwise.setWork(work);

    var identification = OF.createIdentification();
    score_partwise.setIdentification(identification);
    identification.setEncoding(OF.createEncoding());
    var creator = new TypedText();
    identification.getCreator().add(creator);
    creator.setValue(composer);
    creator.setType("composer");
    var today = DatatypeFactory.newDefaultInstance()
            .newXMLGregorianCalendar(Instant.now().toString());
    var encoding_date = OF.createEncodingEncodingDate(today);
    identification.getEncoding().getEncodingDateOrEncoderOrSoftware().add(encoding_date);

    part_list = OF.createPartList();
    score_partwise.setPartList(part_list);

    var treble_melody = new CptMelody[composition.size()];
    var cf_melody = new CptMelody[composition.size()];
    var bass_melody = new CptMelody[composition.size()];

    for (int i = 0; i < composition.size(); i++) {
      var melody = composition.get(i).getValue();
      var tacet = CptMelody.getTacetMelody(melody.length());
      treble_melody[i]
              = composition.get(i).getKey() == CPT_TREBLE ? melody : tacet;
      cf_melody[i]
              = composition.get(i).getKey() == CF ? melody : ((CptCounterpoint) melody).getCf();
      bass_melody[i]
              = composition.get(i).getKey() == CPT_BASS ? melody : tacet;
    }

    var treble = new XmlPart_Motet("P1", "Counterpoint-Treble", "cpt-tr",
            XmlClef.G2, treble_melody);
    var cf = new XmlPart_Motet("P2", "Cantus Firmus", "cf",
            XmlClef.C3, cf_melody);
    var bass = new XmlPart_Motet("P3", "Counterpoint-Bass", "cpt-b",
            XmlClef.F4, bass_melody);

    this.addParts(treble, cf, bass);
  }

  private void addParts(XmlPart_Motet... parts) {

    var part_group_start = OF.createPartGroup();
    part_list.getPartGroupOrScorePart().add(part_group_start);
    part_group_start.setNumber("1");
    part_group_start.setType(StartStop.START);
    var group_symbol = OF.createGroupSymbol();
    part_group_start.setGroupSymbol(group_symbol);
    group_symbol.setValue(GroupSymbolValue.BRACKET);
    var group_barline = OF.createGroupBarline();
    part_group_start.setGroupBarline(group_barline);
    group_barline.setValue(GroupBarlineValue.YES);

    for (XmlPart_Motet part : parts) {
      var score_part = createPart(part);
      part_list.getPartGroupOrScorePart().add(score_part);
      addMelody(score_part, part.getMelodies(), part.getClef());
    }

    var part_group_stop = OF.createPartGroup();
    part_list.getPartGroupOrScorePart().add(part_group_stop);
    part_group_stop.setNumber("1");
    part_group_stop.setType(StartStop.STOP);
  }

  private ScorePart createPart(XmlPart_Motet part) {

    var i_id = part.getId() + "-" + part.getId().replace("P", "I");
    var score_part = OF.createScorePart();
    score_part.setId(part.getId());

    var part_name = OF.createPartName();
    score_part.setPartName(part_name);
    part_name.setValue(part.getPart_name());

    var name_display = OF.createNameDisplay();
    score_part.setPartNameDisplay(name_display);
    var display_text = OF.createFormattedText();
    name_display.getDisplayTextOrAccidentalText().add(display_text);
    display_text.setValue(part.getPart_name().replace("-", "\n"));
    display_text.setFontSize("10");

    var part_abbr = OF.createPartName();
    score_part.setPartAbbreviation(part_abbr);
    part_abbr.setValue(part.getPart_abbreviation());

    var abbr_display = OF.createNameDisplay();
    score_part.setPartAbbreviationDisplay(abbr_display);
    var abbr_display_text = OF.createFormattedText();
    abbr_display.getDisplayTextOrAccidentalText().add(abbr_display_text);
    abbr_display_text.setValue(part.getPart_abbreviation());
    abbr_display_text.setFontSize("10");

    var score_instrument = OF.createScoreInstrument();
    score_instrument.setId(i_id);
    score_instrument.setInstrumentName(INSTRUMENT_NAME);
    score_part.getScoreInstrument().add(score_instrument);

    var midi_instrument = OF.createMidiInstrument();
    midi_instrument.setId(score_instrument);
    midi_instrument.setMidiChannel(Integer.valueOf(part.getId().replace("P", "")));
    midi_instrument.setMidiProgram(MIDI_PROGRAM);
    score_part.getMidiDeviceAndMidiInstrument().add(midi_instrument);

    return score_part;
  }

  private void addMelody(ScorePart score_part,
          List<CptMelody> melodies, XmlClef xml_clef) {

    var part = OF.createScorePartwisePart();
    score_partwise.getPart().add(part);
    part.setId(score_part);

    var measure_no = 0;

    for (CptMelody melody : melodies) {
      var pitch_list = melody.getMelody();
      var start_measure = measure_no + 1;
      var end_measure = measure_no + pitch_list.size();

      for (int i = 0; i < pitch_list.size(); i++) {
        var measure = OF.createScorePartwisePartMeasure();
        part.getMeasure().add(measure);
        measure.setNumber(++measure_no + "");

        if (measure_no == 1) {
          var attributes = OF.createAttributes();
          measure.getNoteOrBackupOrForward().add(attributes);
          attributes.setDivisions(BigDecimal.valueOf(DIVISIONS));

          var key = OF.createKey();
          attributes.getKey().add(key);
          key.setFifths(BigInteger.ZERO);

          var time = OF.createTime();
          attributes.getTime().add(time);
          time.setSymbol(TIME_SYMBOL);
          time.getTimeSignature().add(OF.createTimeBeats("2"));
          time.getTimeSignature().add(OF.createTimeBeatType("2"));

          var clef = OF.createClef();
          attributes.getClef().add(clef);
          clef.setSign(ClefSign.valueOf(xml_clef.getSign().name()));
          clef.setLine(BigInteger.valueOf(xml_clef.getLine()));

          if ("P1".equals(score_part.getId())) {
            var direction = OF.createDirection();
            measure.getNoteOrBackupOrForward().add(direction);
            var direction_type = OF.createDirectionType();
            direction.getDirectionType().add(direction_type);
            var metronome = OF.createMetronome();
            direction_type.setMetronome(metronome);
            metronome.getBeatUnit().add(BEAT_UNIT);
            var per_minute = OF.createPerMinute();
            metronome.setPerMinute(per_minute);
            per_minute.setValue(PER_MINUTE.toString());

            var sound = OF.createSound();
            measure.getNoteOrBackupOrForward().add(sound);
            sound.setTempo(BigDecimal.valueOf(PER_MINUTE * 4));
          }
        } else if (measure_no == start_measure) {
          var print = OF.createPrint();
          measure.getNoteOrBackupOrForward().add(print);
          print.setNewSystem(YesNo.YES);
        }
        var cpt_pitch = pitch_list.get(i).getPitch();
        var note = OF.createNote();
        measure.getNoteOrBackupOrForward().add(note);
        note.setDuration(BigDecimal.valueOf(DURATION));
        if (cpt_pitch == null) {
          note.setPrintObject(YesNo.NO);
          var rest = OF.createRest();
          rest.setMeasure(YesNo.YES);
          note.setRest(rest);
        } else {
          var pitch = OF.createPitch();
          note.setPitch(pitch);
          pitch.setStep(Step.valueOf(cpt_pitch.getStep()));
          pitch.setOctave(cpt_pitch.getOctave());
          var type = OF.createNoteType();
          note.setType(type);
          type.setValue("whole");

          var req_tied = i > 0 && cpt_pitch.equals(pitch_list.get(i - 1).getPitch());

          switch (cpt_pitch.getAccidental()) {
            case "sharp" -> {
              pitch.setAlter(BigDecimal.ONE);
              if (!req_tied) {
                var accidental = OF.createAccidental();
                note.setAccidental(accidental);
                accidental.setValue(AccidentalValue.SHARP);
              }
            }
            case "flat" -> {
              pitch.setAlter(BigDecimal.valueOf(-1));
              if (!req_tied) {
                var accidental = OF.createAccidental();
                note.setAccidental(accidental);
                accidental.setValue(AccidentalValue.FLAT);
              }
            }
            default -> {
              if (!req_tied && IntStream.range(0, i)
                      .mapToObj(pitch_list::get)
                      .map(CptPitchNode::getPitch)
                      .anyMatch(p -> p.getNatural().equals(cpt_pitch) && !p.getAccidental().isBlank())) {
                var accidental = OF.createAccidental();
                note.setAccidental(accidental);
                accidental.setValue(AccidentalValue.NATURAL);
              }
            }
          }

          var notations = OF.createNotations();
          note.getNotations().add(notations);
          if (req_tied) {
            var tie = OF.createTie();
            note.getTie().add(tie);
            tie.setType(StartStop.STOP);
            var tied = OF.createTied();
            tied.setType(StartStopContinue.STOP);
            notations.getTiedOrSlurOrTuplet().add(tied);
          }
          if (i + 1 < pitch_list.size() && cpt_pitch.equals(pitch_list.get(i + 1).getPitch())) {
            var tie = OF.createTie();
            note.getTie().add(tie);
            tie.setType(StartStop.START);
            var tied = OF.createTied();
            tied.setType(StartStopContinue.START);
            notations.getTiedOrSlurOrTuplet().add(tied);
          }
          if (measure_no == end_measure) {
            var fermata = OF.createFermata();
            if ("P3".equals(score_part.getId())) {
              fermata.setType(UprightInverted.INVERTED);
              fermata.setDefaultY(BigDecimal.valueOf(-65));
            } else {
              fermata.setType(UprightInverted.UPRIGHT);
              fermata.setDefaultY(BigDecimal.valueOf(5));
            }
            notations.getTiedOrSlurOrTuplet().add(fermata);
          }
        }
        var barline = OF.createBarline();
        measure.getNoteOrBackupOrForward().add(barline);
        barline.setLocation(RightLeftMiddle.RIGHT);
        var barstyle = OF.createBarStyleColor();
        barline.setBarStyle(barstyle);
        barstyle.setValue(measure_no == end_measure
                ? BarStyle.LIGHT_LIGHT : BarStyle.NONE);
      }
    }
  }

  public void writeMusicXML(File destination) {

    destination.getParentFile().mkdirs();
    try ( var os = new FileOutputStream(destination)) {
      Marshalling.marshal(score_partwise, os, true, 2);
    } catch (MarshallingException | FileNotFoundException ex) {
      Logger.getLogger(MotetScore.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(MotetScore.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
  }
}
