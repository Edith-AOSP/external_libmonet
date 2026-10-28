/*
 * Copyright 2026 The Android Open Source Project
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

package com.google.ux.material.libmonet.palettes;

import static java.lang.Math.abs;

/**
 * Relative adjustments applied to a tonal palette: a chroma multiplier and a tone curve.
 *
 * <p>Percentages use 100 as the unchanged value. Saturation scales the palette chroma and
 * lightness bends the tone before the palette creates a color.
 */
public final class PaletteTuning {
  public static final int NEUTRAL_LEVEL = 100;

  private static final double[][] DARKEST_CURVE = {
    {0.0, 0.0}, {6.0, 0.0}, {12.0, 2.0}, {90.0, 80.0}, {100.0, 100.0}
  };
  private static final double[][] BRIGHTEST_CURVE = {
    {0.0, 0.0}, {10.0, 20.0}, {88.0, 98.0}, {100.0, 100.0}
  };
  private static final double VIVID_SHADES_FACTOR = 1.25;
  private static final double VIVID_TONE_SHIFT = 6.0;
  private static final double VIVID_TAPER_TONES = 10.0;
  private static final double MID_TONE = 50.0;

  private final int saturation;
  private final int lightness;
  private final boolean accurateShades;
  private final boolean vivid;
  private final double chromaFactor;
  private final double toneBlend;

  private PaletteTuning(int saturation, int lightness, boolean accurateShades) {
    this.saturation = saturation;
    this.lightness = lightness;
    this.accurateShades = accurateShades;
    this.vivid = !accurateShades;
    this.chromaFactor =
        saturation / (double) NEUTRAL_LEVEL * (vivid ? VIVID_SHADES_FACTOR : 1.0);
    this.toneBlend = (NEUTRAL_LEVEL - lightness) / (double) NEUTRAL_LEVEL;
  }

  public static PaletteTuning of(int saturation, int lightness) {
    return new PaletteTuning(saturation, lightness, true);
  }

  public static PaletteTuning of(int saturation, int lightness, boolean accurateShades) {
    return new PaletteTuning(saturation, lightness, accurateShades);
  }

  public static PaletteTuning identity() {
    return new PaletteTuning(NEUTRAL_LEVEL, NEUTRAL_LEVEL, true);
  }

  public double getChromaFactor() {
    return chromaFactor;
  }

  public boolean isIdentity() {
    return saturation == NEUTRAL_LEVEL && lightness == NEUTRAL_LEVEL && accurateShades;
  }

  public double getTone(double tone) {
    final double shifted = lightnessTone(tone);
    return vivid ? vividTone(shifted) : shifted;
  }

  private double lightnessTone(double tone) {
    if (lightness == NEUTRAL_LEVEL) {
      return tone;
    }
    final double clamped = Math.max(0.0, Math.min(100.0, tone));
    final double[][] curve = toneBlend > 0 ? DARKEST_CURVE : BRIGHTEST_CURVE;
    return clamped + abs(toneBlend) * (map(curve, clamped) - clamped);
  }

  private static double vividTone(double tone) {
    if (tone <= MID_TONE) {
      return tone;
    }
    final double fromWhite = Math.max(0.0, Math.min(1.0, (100.0 - tone) / VIVID_TAPER_TONES));
    final double fromMid = Math.max(0.0, Math.min(1.0, (tone - MID_TONE) / VIVID_TAPER_TONES));
    return tone - VIVID_TONE_SHIFT * fromWhite * fromMid;
  }

  private static double map(double[][] curve, double tone) {
    int upper = 1;
    while (upper < curve.length - 1 && curve[upper][0] < tone) {
      upper++;
    }
    final double fromTone = curve[upper - 1][0];
    final double toTone = curve[upper][0];
    final double fromValue = curve[upper - 1][1];
    final double toValue = curve[upper][1];
    if (fromTone == toTone) {
      return toValue;
    }
    return fromValue + (toValue - fromValue) * (tone - fromTone) / (toTone - fromTone);
  }
}
