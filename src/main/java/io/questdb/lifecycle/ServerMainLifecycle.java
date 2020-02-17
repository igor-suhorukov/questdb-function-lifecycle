package io.questdb.lifecycle;

import io.questdb.cairo.CairoEngine;
import io.questdb.cutlass.http.HttpServer;
import io.questdb.cutlass.line.udp.AbstractLineProtoReceiver;
import io.questdb.cutlass.pgwire.PGWireServer;
import io.questdb.log.Log;
import io.questdb.mp.WorkerPool;
import io.questdb.plugin.GlobalComponent;
import io.questdb.std.Misc;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ServerMainLifecycle extends io.questdb.ServerMain {

    private List<GlobalComponent> globalComponents;

    public ServerMainLifecycle(String[] args) throws Exception {
        super(args);
    }

    public static void main(String[] args) throws Exception{
        new ServerMainLifecycle(args);
    }

    @Override
    protected void startQuestDb(WorkerPool workerPool, AbstractLineProtoReceiver lineProtocolReceiver, Log log,
                                CairoEngine cairoEngine) {
        globalComponents = getGlobalComponents();
        globalComponents.forEach(lifecycle -> lifecycle.init(cairoEngine));
        super.startQuestDb(workerPool, lineProtocolReceiver, log, cairoEngine);
    }

    @Override
    protected void shutdownQuestDb(WorkerPool workerPool, CairoEngine cairoEngine, HttpServer httpServer,
                                   PGWireServer pgWireServer, AbstractLineProtoReceiver lineProtocolReceiver) {
        super.shutdownQuestDb(workerPool, cairoEngine, httpServer, pgWireServer, lineProtocolReceiver);
        globalComponents.forEach(Misc::free);
    }

    private List<GlobalComponent> getGlobalComponents() {
        ServiceLoader<GlobalComponent> globalComponents = ServiceLoader.load(GlobalComponent.class);
        return StreamSupport.stream(globalComponents.spliterator(), false).collect(Collectors.toList());
    }
}
