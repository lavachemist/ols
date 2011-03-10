/*
 * OpenBench LogicSniffer / SUMP project 
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02110, USA
 *
 * Copyright (C) 2006-2010 Michael Poppitz, www.sump.org
 * Copyright (C) 2010-2011 J.W. Janssen, www.lxtreme.nl
 */
package org.sump.device.logicsniffer;


import java.util.*;
import java.util.logging.*;

import nl.lxtreme.ols.api.data.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Parameterized.Parameters;
import org.sump.device.logicsniffer.profile.*;
import org.sump.device.logicsniffer.profile.DeviceProfile.CaptureClockSource;


/**
 * Test cases for {@link LogicSnifferDevice}.
 */
@RunWith( Parameterized.class )
public class LogicSnifferDeviceComponentTest
{
  // CONSTRUCTORS

  private static final Logger LOG = Logger.getAnonymousLogger();

  // VARIABLES

  private VirtualLogicSnifferDevice device;

  private final int delayCounter;
  private final double ratio;
  private final int readCounter;
  private final int sampleRate;
  private final boolean triggerEnabled;
  private final int enabledChannelsMask;
  private final boolean useRLE;
  private final int expectedSampleValue;

  // CONSTANTS

  /**
   * Creates a new LogicSnifferDeviceTest instance.
   */
  public LogicSnifferDeviceComponentTest( final int aSampleRate, final boolean aTriggerEnabled, final double aRatio,
      final int aReadCounter, final int aDelayCounter, final int aEnabledChannelsMask, final boolean aUseRLE,
      final int aExpectedSampleValue )
  {
    this.delayCounter = aDelayCounter;
    this.ratio = aRatio;
    this.sampleRate = aSampleRate;
    this.triggerEnabled = aTriggerEnabled;
    this.readCounter = aReadCounter;
    this.enabledChannelsMask = aEnabledChannelsMask;
    this.useRLE = aUseRLE;
    this.expectedSampleValue = aExpectedSampleValue;
  }

  // METHODS

  /**
   * @return a collection of test data.
   */
  @Parameters
  @SuppressWarnings( "boxing" )
  public static Collection<Object[]> getTestData()
  {
    return Arrays.asList( new Object[][] { //
        // { sample rate, trigger?, ratio, read counter, delay counter, enabled
        // channels mask, rle?, expected sample value }
            { 100000000, true, 1.00, 1024, 1024, 0x000000FF, false, 0x000000FF }, // 0
            { 100000000, true, 0.50, 1024, 512, 0x000000FF, false, 0x000000FF }, // 1
            { 100000000, true, 0.25, 1024, 256, 0x000000FF, false, 0x000000FF }, // 2
            { 100000000, true, 0.25, 1024, 256, 0xFF00FF00, false, 0xFF00FF00 }, // 3
            { 200000000, true, 0.25, 1024, 256, 0xFFFFFFFF, false, 0x0000FFFF }, // 4
            { 100000000, true, 0.75, 1024, 768, 0x000000FF, true, 0x0000007F }, // 5
            { 100000000, true, 0.75, 1024, 768, 0x0000FF00, true, 0x00007F00 }, // 6
            { 100000000, true, 0.75, 1024, 768, 0x0000FFFF, true, 0x00007F7F }, // 7
            { 100000000, true, 0.75, 1024, 768, 0x00FF0000, true, 0x007F0000 }, // 8
            { 100000000, true, 0.75, 1024, 768, 0x00FF00FF, true, 0x007F007F }, // 9
            { 100000000, true, 0.75, 1024, 768, 0x00FFFFFF, true, 0x007F7F7F }, // 10
            { 100000000, true, 0.75, 1024, 768, 0xFF000000, true, 0x7F000000 }, // 11
            { 100000000, true, 0.75, 1024, 768, 0xFF0000FF, true, 0x7F00007F }, // 12
            { 100000000, true, 0.75, 1024, 768, 0xFF00FF00, true, 0x7F007F00 }, // 13
            { 100000000, true, 0.75, 1024, 768, 0xFF00FFFF, true, 0x7F007F7F }, // 14
            { 100000000, true, 0.75, 1024, 768, 0xFFFF0000, true, 0x7F7F0000 }, // 15
            { 100000000, true, 0.75, 1024, 768, 0xFFFF00FF, true, 0x7F7F007F }, // 16
            { 100000000, true, 1.00, 1024, 1024, 0xFFFFFFFF, true, 0x7F7F7F7F }, // 17
            { 200000000, true, 0.25, 1024, 256, 0xFF00FF00, true, 0x00007F00 }, // 18
            { 200000000, true, 0.25, 1024, 256, 0xFFFFFFFF, true, 0x00007F7F }, // 19
        } );
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception
  {
    LogicSnifferConfig config = new LogicSnifferConfig();
    this.device = new VirtualLogicSnifferDevice( config );

    final DeviceProfile deviceProfile = this.device.addDeviceProfile( "VirtualLS", "\"Virtual LogicSniffer\"" );
    config.setDeviceProfile( deviceProfile );

    config.setAltNumberSchemeEnabled( false ); // don't care
    config.setBaudrate( 9600 ); // don't care
    config.setPortName( "/dev/virtual" ); // don't care
    config.setClockSource( CaptureClockSource.INTERNAL ); // don't care
    config.setFilterEnabled( true ); // don't care
    config.setTestModeEnabled( false ); // don't care
    config.setEnabledChannels( this.enabledChannelsMask );
    config.setRatio( this.ratio );
    config.setRleEnabled( this.useRLE );
    config.setSampleCount( this.readCounter );
    config.setSampleRate( this.sampleRate );
    config.setTriggerEnabled( this.triggerEnabled );

    LOG.log( Level.INFO, "Sample rate: {0}Hz; Read counter: {1}; Delay counter: {2}; Ratio: {3}; "
        + "Trigger: {4}; Enabled channels: {5}; Use RLE: {6}", new Object[] { this.sampleRate, this.readCounter,
        this.delayCounter, this.ratio, this.triggerEnabled, Integer.toHexString( this.enabledChannelsMask ),
        this.useRLE } );
  }

  /**
   * @throws Exception
   */
  @After
  public void tearDown() throws Exception
  {
    this.device.close();
  }

  /**
   * Test method for
   * {@link org.sump.device.logicsniffer.LogicSnifferDevice#doInBackground()}.
   */
  @Test( timeout = 6000 )
  public void testVerifyFlags() throws Exception
  {
    final boolean ddrMode = ( this.sampleRate > this.device.getConfig().getClockspeed() );
    final boolean isGroup1disabled = ( this.enabledChannelsMask & 0x000000FF ) == 0;
    final boolean isGroup2disabled = ( this.enabledChannelsMask & 0x0000FF00 ) == 0;
    final boolean isGroup3disabled = ( ( this.enabledChannelsMask & 0x00FF0000 ) == 0 )
        || ( ddrMode && isGroup1disabled );
    final boolean isGroup4disabled = ( ( this.enabledChannelsMask & 0xFF000000 ) == 0 )
        || ( ddrMode && isGroup2disabled );

    this.device.doInBackground();

    this.device.assertFlagState( LogicSnifferDevice.FLAG_DEMUX, ddrMode );
    this.device.assertFlagState( LogicSnifferDevice.FLAG_GROUP1_DISABLED, isGroup1disabled );
    this.device.assertFlagState( LogicSnifferDevice.FLAG_GROUP2_DISABLED, isGroup2disabled );
    this.device.assertFlagState( LogicSnifferDevice.FLAG_GROUP3_DISABLED, isGroup3disabled );
    this.device.assertFlagState( LogicSnifferDevice.FLAG_GROUP4_DISABLED, isGroup4disabled );
    this.device.assertFlagState( LogicSnifferDevice.FLAG_RLE, this.useRLE );
    // The following values are static/not used...
    this.device.assertFlagState( LogicSnifferDevice.FLAG_INVERTED, false );
    this.device.assertFlagState( LogicSnifferDevice.FLAG_EXTERNAL, false );
    this.device.assertFlagState( LogicSnifferDevice.FLAG_EXTERNAL_TEST_MODE, false );
    this.device.assertFlagState( LogicSnifferDevice.FLAG_INTERNAL_TEST_MODE, false );
    this.device.assertFlagState( LogicSnifferDevice.FLAG_NUMBER_SCHEME, false );
    this.device.assertFlagState( LogicSnifferDevice.FLAG_FILTER, false );
  }

  /**
   * Test method for
   * {@link org.sump.device.logicsniffer.LogicSnifferDevice#doInBackground()}.
   */
  @Test( timeout = 6000 )
  public void testVerifySentData() throws Exception
  {
    final AcquisitionResult result = this.device.doInBackground();

    this.device.assertSampleRate( this.sampleRate );
    this.device.assertReadAndDelayCount( this.readCounter, this.delayCounter );
    this.device.assertConstantDataStream( result, this.expectedSampleValue, this.readCounter );
  }
}