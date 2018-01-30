package org.opennms.netmgt.telemetry.listeners.sflow.proto.flows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import com.google.common.primitives.UnsignedInteger;
import com.google.common.primitives.UnsignedLong;

import org.opennms.netmgt.telemetry.listeners.api.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.sflow.InvalidPacketException;
import org.opennms.netmgt.telemetry.listeners.sflow.proto.*;

// struct tokenring_counters {
//   unsigned int dot5StatsLineErrors;
//   unsigned int dot5StatsBurstErrors;
//   unsigned int dot5StatsACErrors;
//   unsigned int dot5StatsAbortTransErrors;
//   unsigned int dot5StatsInternalErrors;
//   unsigned int dot5StatsLostFrameErrors;
//   unsigned int dot5StatsReceiveCongestions;
//   unsigned int dot5StatsFrameCopiedErrors;
//   unsigned int dot5StatsTokenErrors;
//   unsigned int dot5StatsSoftErrors;
//   unsigned int dot5StatsHardErrors;
//   unsigned int dot5StatsSignalLoss;
//   unsigned int dot5StatsTransmitBeacons;
//   unsigned int dot5StatsRecoverys;
//   unsigned int dot5StatsLobeWires;
//   unsigned int dot5StatsRemoves;
//   unsigned int dot5StatsSingles;
//   unsigned int dot5StatsFreqErrors;
// };

public class TokenringCounters  {
  public final UnsignedInteger dot5StatsLineErrors;
  public final UnsignedInteger dot5StatsBurstErrors;
  public final UnsignedInteger dot5StatsACErrors;
  public final UnsignedInteger dot5StatsAbortTransErrors;
  public final UnsignedInteger dot5StatsInternalErrors;
  public final UnsignedInteger dot5StatsLostFrameErrors;
  public final UnsignedInteger dot5StatsReceiveCongestions;
  public final UnsignedInteger dot5StatsFrameCopiedErrors;
  public final UnsignedInteger dot5StatsTokenErrors;
  public final UnsignedInteger dot5StatsSoftErrors;
  public final UnsignedInteger dot5StatsHardErrors;
  public final UnsignedInteger dot5StatsSignalLoss;
  public final UnsignedInteger dot5StatsTransmitBeacons;
  public final UnsignedInteger dot5StatsRecoverys;
  public final UnsignedInteger dot5StatsLobeWires;
  public final UnsignedInteger dot5StatsRemoves;
  public final UnsignedInteger dot5StatsSingles;
  public final UnsignedInteger dot5StatsFreqErrors;

  public TokenringCounters (final ByteBuffer buffer) throws InvalidPacketException {
    this.dot5StatsLineErrors = BufferUtils.uint32(buffer);
    this.dot5StatsBurstErrors = BufferUtils.uint32(buffer);
    this.dot5StatsACErrors = BufferUtils.uint32(buffer);
    this.dot5StatsAbortTransErrors = BufferUtils.uint32(buffer);
    this.dot5StatsInternalErrors = BufferUtils.uint32(buffer);
    this.dot5StatsLostFrameErrors = BufferUtils.uint32(buffer);
    this.dot5StatsReceiveCongestions = BufferUtils.uint32(buffer);
    this.dot5StatsFrameCopiedErrors = BufferUtils.uint32(buffer);
    this.dot5StatsTokenErrors = BufferUtils.uint32(buffer);
    this.dot5StatsSoftErrors = BufferUtils.uint32(buffer);
    this.dot5StatsHardErrors = BufferUtils.uint32(buffer);
    this.dot5StatsSignalLoss = BufferUtils.uint32(buffer);
    this.dot5StatsTransmitBeacons = BufferUtils.uint32(buffer);
    this.dot5StatsRecoverys = BufferUtils.uint32(buffer);
    this.dot5StatsLobeWires = BufferUtils.uint32(buffer);
    this.dot5StatsRemoves = BufferUtils.uint32(buffer);
    this.dot5StatsSingles = BufferUtils.uint32(buffer);
    this.dot5StatsFreqErrors = BufferUtils.uint32(buffer);
  }
}
