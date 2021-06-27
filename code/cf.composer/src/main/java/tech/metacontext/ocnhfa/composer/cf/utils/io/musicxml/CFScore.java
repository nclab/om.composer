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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.String;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.util.List;
import java.util.logging.Logger;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeFactory;
import org.audiveris.proxymusic.*;
import org.audiveris.proxymusic.ScorePartwise.Part;
import org.audiveris.proxymusic.util.Marshalling;
import org.audiveris.proxymusic.util.Marshalling.MarshallingException;
import tech.metacontext.ocnhfa.composer.cf.model.y.PitchNode;

/**
 *
 * @author Jonathan Chang, Chun-yien <ccy@musicapoetica.org>
 */
public class CFScore {

  private static final ObjectFactory OF = new ObjectFactory();

  private final ScorePartwise score_partwise;
  private final PartList part_list;
  private final ScorePart score_part;
  private final Part part;

  private static final String PART_ID = "P1";
  private static final String INSTRUMENT_ID = "P1-I1";
  private static final String PART_NAME = "Cantus Firmus";
  private static final String PART_ABBR = "cf";

  private static final TimeSymbol TIME_SYMBOL = TimeSymbol.CUT;
  private static final int DIVISIONS = 1;
  private static final int DURATION = 4;
  private static final String INSTRUMENT_NAME = "Church Organ";
  private static final int MIDI_CHANNEL = 1;
  private static final int MIDI_PROGRAM = 20;

  private static final String BEAT_UNIT = "whole";
  private static final Integer PER_MINUTE = 105;

  static {
    try {
      Marshalling.getContext(ScorePartwise.class);
    } catch (JAXBException ex) {
      Logger.getLogger(CFScore.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
  }

  public CFScore(String title, String composer) {

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

    var part_group_start = OF.createPartGroup();
    part_list.getPartGroupOrScorePart().add(part_group_start);
    part_group_start.setNumber("1");
    part_group_start.setType(StartStop.START);
    var group_symbol = OF.createGroupSymbol();
    part_group_start.setGroupSymbol(group_symbol);
    group_symbol.setValue(GroupSymbolValue.BRACKET);

    score_part = OF.createScorePart();
    part_list.getPartGroupOrScorePart().add(score_part);
    score_part.setId(PART_ID);

    part = OF.createScorePartwisePart();
    score_partwise.getPart().add(part);
    part.setId(score_part);
    createPart();

    var part_group_stop = OF.createPartGroup();
    part_list.getPartGroupOrScorePart().add(part_group_stop);
    part_group_stop.setNumber("1");
    part_group_stop.setType(StartStop.STOP);

  }

  private void createPart() {

    var part_name = OF.createPartName();
    score_part.setPartName(part_name);
    part_name.setValue(PART_NAME);

    var name_display = OF.createNameDisplay();
    score_part.setPartNameDisplay(name_display);
    var display_text = OF.createFormattedText();
    name_display.getDisplayTextOrAccidentalText().add(display_text);
    display_text.setValue(PART_NAME);
    display_text.setFontSize("10");

    var part_abbr = OF.createPartName();
    score_part.setPartAbbreviation(part_abbr);
    part_abbr.setValue(PART_ABBR);

    var abbr_display = OF.createNameDisplay();
    score_part.setPartAbbreviationDisplay(abbr_display);
    var abbr_display_text = OF.createFormattedText();
    abbr_display.getDisplayTextOrAccidentalText().add(abbr_display_text);
    abbr_display_text.setValue(PART_ABBR);
    abbr_display_text.setFontSize("10");

    var score_instrument = OF.createScoreInstrument();
    score_instrument.setId(INSTRUMENT_ID);
    score_instrument.setInstrumentName(INSTRUMENT_NAME);
    score_part.getScoreInstrument().add(score_instrument);

    var midi_instrument = OF.createMidiInstrument();
    midi_instrument.setId(score_instrument);
    midi_instrument.setMidiChannel(MIDI_CHANNEL);
    midi_instrument.setMidiProgram(MIDI_PROGRAM);
    score_part.getMidiDeviceAndMidiInstrument().add(midi_instrument);

  }

  public void addMeasure(Clef selected_clef, Integer measure_no,
          List<PitchNode> melody) {

    var measure = OF.createScorePartwisePartMeasure();
    part.getMeasure().add(measure);
    measure.setNumber(measure_no.toString());

    var attributes = OF.createAttributes();
    measure.getNoteOrBackupOrForward().add(attributes);
    attributes.setDivisions(BigDecimal.valueOf(DIVISIONS));

    if (measure_no == 1) {

      var key = OF.createKey();
      attributes.getKey().add(key);
      key.setFifths(BigInteger.ZERO);

      var time = OF.createTime();
      attributes.getTime().add(time);
      time.setSymbol(TIME_SYMBOL);
      time.getTimeSignature().add(OF.createTimeBeats("2"));
      time.getTimeSignature().add(OF.createTimeBeatType("2"));

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

    var clef = OF.createClef();
    attributes.getClef().add(clef);
    clef.setSign(ClefSign.fromValue(selected_clef.sign));
    clef.setLine(new BigInteger(selected_clef.line));
    for (int i = 0; i < melody.size(); i++) {

      var print = OF.createPrint();
      measure.getNoteOrBackupOrForward().add(print);
      print.setNewSystem(YesNo.YES);

      var cf_pitch = melody.get(i).getPitch();
      var note = OF.createNote();
      measure.getNoteOrBackupOrForward().add(note);
      note.setDuration(BigDecimal.valueOf(DURATION));

      var pitch = OF.createPitch();
      note.setPitch(pitch);
      pitch.setStep(cf_pitch.getStep());
      pitch.setOctave(cf_pitch.getOctave());
      var type = OF.createNoteType();
      note.setType(type);
      type.setValue("whole");

      var notations = OF.createNotations();
      note.getNotations().add(notations);
      if (i > 0 && cf_pitch.equals(melody.get(i - 1).getPitch())) {
        var tie = OF.createTie();
        note.getTie().add(tie);
        tie.setType(StartStop.STOP);
        var tied = OF.createTied();
        tied.setType(StartStopContinue.STOP);
        notations.getTiedOrSlurOrTuplet().add(tied);
      }
      if (i + 1 < melody.size() && cf_pitch.equals(melody.get(i + 1).getPitch())) {
        var tie = OF.createTie();
        note.getTie().add(tie);
        tie.setType(StartStop.START);
        var tied = OF.createTied();
        tied.setType(StartStopContinue.START);
        notations.getTiedOrSlurOrTuplet().add(tied);
      }
      if (i == melody.size() - 1) {
        var fermata = OF.createFermata();
        fermata.setType(UprightInverted.UPRIGHT);
        fermata.setDefaultY(BigDecimal.valueOf(5));
        notations.getTiedOrSlurOrTuplet().add(fermata);
      }
    }
    var barline = OF.createBarline();
    measure.getNoteOrBackupOrForward().add(barline);
    barline.setLocation(RightLeftMiddle.RIGHT);
    var barstyle = OF.createBarStyleColor();
    barline.setBarStyle(barstyle);
    barstyle.setValue(BarStyle.LIGHT_LIGHT);
  }

  public void writeMusicXML(File destination) {

    destination.getParentFile().mkdirs();
    try (var os = new FileOutputStream(destination)) {
      Marshalling.marshal(score_partwise, os, true, 2);
    } catch (MarshallingException | FileNotFoundException ex) {
      Logger.getLogger(CFScore.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(CFScore.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }
  }
}
