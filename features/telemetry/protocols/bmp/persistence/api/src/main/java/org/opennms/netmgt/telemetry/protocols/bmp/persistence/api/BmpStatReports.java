/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.telemetry.protocols.bmp.persistence.api;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

public class BmpStatReports implements Serializable {

    private static final long serialVersionUID = 7692456270634611593L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bmpStatsReportsSequence")
    @SequenceGenerator(name = "bmpStatsReportsSequence", sequenceName = "bmpstatreportsnxtid")
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "peer_hash_id", nullable = false)
    private String peerHashId;

    @Column(name = "prefixes_rejected")
    private Integer prefixesRejected;

    @Column(name = "known_dup_prefixes")
    private Integer knownDupPrefixes;

    @Column(name = "known_dup_withdraws")
    private Integer knownDupWithdraws;

    @Column(name = "updates_invalid_by_cluster_list")
    private Integer updatesInvalidByClusterList;

    @Column(name = "updates_invalid_by_as_path_loop")
    private Integer updatesInvalidByAsPathLoop;

    @Column(name = "updates_invalid_by_originator_id")
    private Integer updatesInvalidByOriginatorId;

    @Column(name = "updates_invalid_by_as_confed_loop")
    private Integer updatesInvalidByAsConfedLoop;

    @Column(name = "num_routes_adj_rib_in")
    private Long numRoutesAdjRibIn;

    @Column(name = "num_routes_local_rib")
    private Long numROutesLocalRib;

    @Column(name = "timestamp")
    private Date timestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPeerHashId() {
        return peerHashId;
    }

    public void setPeerHashId(String peerHashId) {
        this.peerHashId = peerHashId;
    }

    public Integer getPrefixesRejected() {
        return prefixesRejected;
    }

    public void setPrefixesRejected(Integer prefixesRejected) {
        this.prefixesRejected = prefixesRejected;
    }

    public Integer getKnownDupPrefixes() {
        return knownDupPrefixes;
    }

    public void setKnownDupPrefixes(Integer knownDupPrefixes) {
        this.knownDupPrefixes = knownDupPrefixes;
    }

    public Integer getKnownDupWithdraws() {
        return knownDupWithdraws;
    }

    public void setKnownDupWithdraws(Integer knownDupWithdraws) {
        this.knownDupWithdraws = knownDupWithdraws;
    }

    public Integer getUpdatesInvalidByClusterList() {
        return updatesInvalidByClusterList;
    }

    public void setUpdatesInvalidByClusterList(Integer updatesInvalidByClusterList) {
        this.updatesInvalidByClusterList = updatesInvalidByClusterList;
    }

    public Integer getUpdatesInvalidByAsPathLoop() {
        return updatesInvalidByAsPathLoop;
    }

    public void setUpdatesInvalidByAsPathLoop(Integer updatesInvalidByAsPathLoop) {
        this.updatesInvalidByAsPathLoop = updatesInvalidByAsPathLoop;
    }

    public Integer getUpdatesInvalidByOriginatorId() {
        return updatesInvalidByOriginatorId;
    }

    public void setUpdatesInvalidByOriginatorId(Integer updatesInvalidByOriginatorId) {
        this.updatesInvalidByOriginatorId = updatesInvalidByOriginatorId;
    }

    public Integer getUpdatesInvalidByAsConfedLoop() {
        return updatesInvalidByAsConfedLoop;
    }

    public void setUpdatesInvalidByAsConfedLoop(Integer updatesInvalidByAsConfedLoop) {
        this.updatesInvalidByAsConfedLoop = updatesInvalidByAsConfedLoop;
    }

    public Long getNumRoutesAdjRibIn() {
        return numRoutesAdjRibIn;
    }

    public void setNumRoutesAdjRibIn(Long numRoutesAdjRibIn) {
        this.numRoutesAdjRibIn = numRoutesAdjRibIn;
    }

    public Long getNumROutesLocalRib() {
        return numROutesLocalRib;
    }

    public void setNumROutesLocalRib(Long numROutesLocalRib) {
        this.numROutesLocalRib = numROutesLocalRib;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
