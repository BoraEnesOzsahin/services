package com.ayrotek.reckon.hiveosintegration.service;

import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosAlgo;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosCoin;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosMiner;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosPoolTemplate;
import com.ayrotek.reckon.hiveosintegration.dto.StartMiningRequest;
import com.ayrotek.reckon.hiveosintegration.dto.StartMiningResponse;
import com.ayrotek.reckon.hiveosintegration.dto.StopMiningRequest;

import java.util.List;

public interface MiningService {

    StartMiningResponse startMining(Long farmId, StartMiningRequest request);

    void stopMining(Long farmId, StopMiningRequest request);

    List<HiveosPoolTemplate> getPoolsForCoin(String coin);

    // --- Strategy builder discovery ---

    List<HiveosCoin> getCoins();

    List<HiveosMiner> getMiners();

    List<HiveosAlgo> getAlgos();
}
