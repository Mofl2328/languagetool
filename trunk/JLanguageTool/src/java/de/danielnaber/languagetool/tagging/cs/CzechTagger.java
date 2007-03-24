/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package de.danielnaber.languagetool.tagging.cs;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;

import com.dawidweiss.stemmers.Lametyzator;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.tagging.Tagger;
import de.danielnaber.languagetool.tools.Tools;
import de.danielnaber.languagetool.JLanguageTool;

/**
 * Czech POS tagger based on FSA morphological dictionaries.
 * 
 * @author Jozef Licko
 */
public class CzechTagger implements Tagger {

  private static final String RESOURCE_FILENAME = "resource" + File.separator + "cs"
      + File.separator + "czech.dict";

  private Lametyzator morfologik = null;

  public final List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens)
      throws IOException {
    String[] taggerTokens;

    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<AnalyzedTokenReadings>();
    int pos = 0;
    //caching Lametyzator instance - lazy init
    if (morfologik == null) {
      File resourceFile = JLanguageTool.getAbsoluteFile(RESOURCE_FILENAME);
      morfologik = new Lametyzator(Tools.getInputStream(resourceFile.getAbsolutePath()),
          "iso8859-2", '+', true, true);
    }

    for (Iterator<String> iter = sentenceTokens.iterator(); iter.hasNext();) {
      String word = iter.next();
      List<AnalyzedToken> l = new ArrayList<AnalyzedToken>();
      String[] lowerTaggerTokens = null;
      taggerTokens = morfologik.stemAndForm(word);
      if (word != word.toLowerCase()) {
        lowerTaggerTokens = morfologik.stemAndForm(word.toLowerCase());
      }

      if (taggerTokens != null) {
        // Lametyzator returns data as String[]
        // first lemma, then annotations
        /*
         if(taggerTokens.length > 2) {
           for (String currStr : taggerTokens)
           System.out.print(currStr + " ");
         System.out.println();
         }
         */
        String lemma = new String();
        int i = 0;
        while (i < taggerTokens.length) {
          // Czech POS tags:
          // If there are multiple tags, they behave as one, i.e. they
          // are connected
          // on one line with '+' character
          lemma = taggerTokens[i];
          String[] tagsArr = taggerTokens[i + 1].split("\\+");

          for (String currTag : tagsArr)
            l.add(new AnalyzedToken(word, currTag, lemma));

          i += 2;
        }
      }

      if (lowerTaggerTokens != null) {

        String lemma = new String();
        int i = 0;
        while (i < lowerTaggerTokens.length) {
          // Czech POS tags again
          lemma = lowerTaggerTokens[i];
          String[] tagsArr = lowerTaggerTokens[i + 1].split("\\+");

          for (String currTag : tagsArr)
            l.add(new AnalyzedToken(word, currTag, lemma));

          i += 2;
        }
      }

      if (lowerTaggerTokens == null && taggerTokens == null) {
        l.add(new AnalyzedToken(word, null, pos));
      }
      pos += word.length();
      tokenReadings
          .add(new AnalyzedTokenReadings((AnalyzedToken[]) l.toArray(new AnalyzedToken[0])));
    }

    return tokenReadings;

  }

  public Object createNullToken(final String token, final int startPos) {
    return new AnalyzedTokenReadings(new AnalyzedToken(token, null, startPos));
  }

}
