package com.ayrotek.reckon.hiveosintegration.service.impl;

import com.ayrotek.reckon.hiveosintegration.client.HiveosApiClient;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosAlgo;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosCoin;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosCommandRequest;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosMiner;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosFlightSheet;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosFlightSheetCreateRequest;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosFsItem;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosPoolTemplate;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosWallet;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosWalletCreateRequest;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosWorker;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosWorkersCommandRequest;
import com.ayrotek.reckon.hiveosintegration.client.dto.HiveosWorkersEditRequest;
import com.ayrotek.reckon.hiveosintegration.dto.StartMiningRequest;
import com.ayrotek.reckon.hiveosintegration.dto.StartMiningResponse;
import com.ayrotek.reckon.hiveosintegration.dto.StopMiningRequest;
import com.ayrotek.reckon.hiveosintegration.exception.MiningConfigurationException;
import com.ayrotek.reckon.hiveosintegration.service.MiningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class MiningServiceImpl implements MiningService {

    private final HiveosApiClient hiveosApiClient;

    @Override
    public StartMiningResponse startMining(Long farmId, StartMiningRequest request) {
        String coin = request.coin().toUpperCase(Locale.ROOT);
        String algo = resolveAlgo(coin, request.algo());
        String pool = request.pool().toLowerCase(Locale.ROOT);

        Long walletId = resolveWallet(farmId, coin, request.walletAddress());
        List<String> poolUrls = resolvePoolUrls(coin, pool, request);
        HiveosFlightSheet flightSheet = resolveFlightSheet(farmId, coin, algo, pool, walletId, poolUrls, request);
        List<Long> workerIds = resolveWorkerIds(farmId, request.workerIds());

        hiveosApiClient.editWorkers(farmId, new HiveosWorkersEditRequest(workerIds, Map.of("fs_id", flightSheet.id())));
        log.info("Applied flight sheet {} ({}) to workers {} on farm {}",
                flightSheet.id(), flightSheet.name(), workerIds, farmId);

        // Applying the flight sheet alone does not reliably (re)start mining: HiveOS keeps a
        // manually stopped miner stopped, and reusing an unchanged fs_id fires no change event.
        // "restart" guarantees the miner comes up on the current flight sheet regardless of prior state
        // ("start" is a no-op when the miner is already running and would skip the new config).
        hiveosApiClient.executeWorkersCommand(farmId, new HiveosWorkersCommandRequest(
                workerIds,
                new HiveosCommandRequest("miner", Map.of("action", "restart"))
        ));
        log.info("Restarted miner on workers {} on farm {}", workerIds, farmId);

        return new StartMiningResponse(walletId, flightSheet.id(), flightSheet.name(), poolUrls, workerIds);
    }

    @Override
    public void stopMining(Long farmId, StopMiningRequest request) {
        List<Long> workerIds = resolveWorkerIds(farmId, request != null ? request.workerIds() : null);
        hiveosApiClient.executeWorkersCommand(farmId, new HiveosWorkersCommandRequest(
                workerIds,
                new HiveosCommandRequest("miner", Map.of("action", "stop"))
        ));
        log.info("Sent miner stop to workers {} on farm {}", workerIds, farmId);
    }

    @Override
    public List<HiveosPoolTemplate> getPoolsForCoin(String coin) {
        return hiveosApiClient.getPoolsByCoin(coin.toUpperCase(Locale.ROOT)).data();
    }

    @Override
    public List<HiveosCoin> getCoins() {
        return hiveosApiClient.getCoins().data();
    }

    @Override
    public List<HiveosMiner> getMiners() {
        return hiveosApiClient.getMiners().data();
    }

    @Override
    public List<HiveosAlgo> getAlgos() {
        return hiveosApiClient.getAlgos().data();
    }

    private Long resolveWallet(Long farmId, String coin, String walletAddress) {
        return hiveosApiClient.getWallets(farmId).data().stream()
                .filter(w -> coin.equalsIgnoreCase(w.coin()) && walletAddress.equals(w.wal()))
                .map(HiveosWallet::id)
                .findFirst()
                .orElseGet(() -> {
                    HiveosWallet created = hiveosApiClient.createWallet(farmId,
                            new HiveosWalletCreateRequest(coin, "remote-" + coin.toLowerCase(Locale.ROOT), walletAddress));
                    log.info("Created wallet {} for coin {} on farm {}", created.id(), coin, farmId);
                    return created.id();
                });
    }

    private List<String> resolvePoolUrls(String coin, String pool, StartMiningRequest request) {
        if (request.poolUrls() != null && !request.poolUrls().isEmpty()) {
            return request.poolUrls();
        }

        boolean ssl = Boolean.TRUE.equals(request.poolSsl());
        return hiveosApiClient.getPoolsByCoin(coin).data().stream()
                .filter(t -> pool.equalsIgnoreCase(t.pool()))
                .findFirst()
                .map(t -> t.props().servers().stream()
                        .map(s -> ssl && s.sslUrls() != null && !s.sslUrls().isEmpty() ? s.sslUrls() : s.urls())
                        .filter(Objects::nonNull)
                        .flatMap(List::stream)
                        .toList())
                .filter(urls -> !urls.isEmpty())
                .orElseThrow(() -> new MiningConfigurationException(
                        "No pool template found for coin=" + coin + " pool=" + pool
                                + ". Check GET /api/v1/hiveos/pools/" + coin + " for available pools,"
                                + " or pass poolUrls explicitly."));
    }

    private String resolveAlgo(String coin, String requestedAlgo) {
        if (requestedAlgo != null && !requestedAlgo.isBlank()) {
            return requestedAlgo.toLowerCase(Locale.ROOT);
        }

        return hiveosApiClient.getCoins().data().stream()
                .filter(c -> coin.equalsIgnoreCase(c.coin()))
                .map(HiveosCoin::algos)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(algo -> algo != null && !algo.isBlank())
                .findFirst()
                .map(algo -> algo.toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new MiningConfigurationException(
                        "No HiveOS algorithm found for coin=" + coin
                                + ". Pick a coin from GET /api/v1/hiveos/coins or pass algo explicitly."));
    }

    private HiveosFlightSheet resolveFlightSheet(Long farmId, String coin, String algo, String pool, Long walletId,
                                                 List<String> poolUrls, StartMiningRequest request) {
        String fsName = "remote-%s-%s-%s".formatted(coin.toLowerCase(Locale.ROOT), pool, request.miner());

        return hiveosApiClient.getFlightSheets(farmId).data().stream()
                .filter(fs -> fsName.equals(fs.name()))
                .filter(fs -> fs.items() != null && fs.items().stream()
                        .anyMatch(i -> Objects.equals(i.walId(), walletId)))
                .findFirst()
                .orElseGet(() -> {
                    HiveosFsItem item = new HiveosFsItem(
                            coin,
                            pool,
                            request.poolSsl(),
                            poolUrls,
                            walletId,
                            request.miner(),
                            minerConfig(algo, request.minerConfig())
                    );
                    log.info("Creating HiveOS flight sheet '{}' with coin={} algo={} pool={} miner={}",
                            fsName, coin, algo, pool, request.miner());
                    HiveosFlightSheet created = hiveosApiClient.createFlightSheet(farmId,
                            new HiveosFlightSheetCreateRequest(fsName, List.of(item)));
                    log.info("Created flight sheet {} ({}) on farm {}", created.id(), fsName, farmId);
                    return created;
                });
    }

    /**
     * Minimal miner config using HiveOS placeholder templates that most miners understand
     * (%URL%, %WAL%, %WORKER_NAME% are substituted by HiveOS itself).
     */
    private Map<String, Object> minerConfig(String algo, Map<String, Object> requested) {
        Map<String, Object> config = new LinkedHashMap<>();
        if (requested != null) {
            config.putAll(requested);
        }
        config.putIfAbsent("url", "%URL%");
        config.putIfAbsent("algo", algo);
        config.putIfAbsent("pass", "x");
        config.putIfAbsent("worker", "%WORKER_NAME%");
        config.putIfAbsent("template", "%WAL%");
        return config;
    }

    private List<Long> resolveWorkerIds(Long farmId, List<Long> requested) {
        if (requested != null && !requested.isEmpty()) {
            return requested;
        }
        List<Long> all = hiveosApiClient.getWorkers(farmId).data().stream()
                .map(HiveosWorker::id)
                .toList();
        if (all.isEmpty()) {
            throw new MiningConfigurationException("Farm " + farmId + " has no workers to mine on.");
        }
        return all;
    }
}
