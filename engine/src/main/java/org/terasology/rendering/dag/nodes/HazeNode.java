/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.rendering.dag.nodes;

import org.terasology.config.Config;
import org.terasology.config.RenderingConfig;
import org.terasology.context.Context;
import org.terasology.engine.SimpleUri;
import org.terasology.rendering.opengl.FBO;
import org.terasology.rendering.world.WorldRenderer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 * This class is a thin facade in front of the BlurNode class it inherits from.
 *
 * Instances of this class specialize the blur operation to render a "Haze" layer,
 * combined later in the pipeline to progressively fade the rendered world into
 * the backdrop.
 *
 * I.e. if the sky is pink at sunset, faraway hills will fade into pink as they get
 * further away from the camera.
 */
public class HazeNode extends BlurNode implements PropertyChangeListener {
    public static final SimpleUri INTERMEDIATE_HAZE_FBO_URI = new SimpleUri("engine:fbo.intermediateHaze");
    public static final SimpleUri FINAL_HAZE_FBO_URI = new SimpleUri("engine:fbo.finalHaze");
    private static final float BLUR_RADIUS = 8.0f;

    private RenderingConfig renderingConfig;

    private WorldRenderer worldRenderer;

    /**
     * Initializes the HazeNode instance.
     *
     * @param inputFbo The input fbo, containing the image to be blurred.
     * @param outputFbo The output fbo, to store the blurred image.
     */
    public HazeNode(Context context, FBO inputFbo, FBO outputFbo) {
        super(context, inputFbo, outputFbo, BLUR_RADIUS);
    }

    /**
     * This method establishes the conditions in which the blur will take place, by enabling or disabling the node.
     *
     * In this particular case the node is enabled if RenderingConfig.isInscattering() returns true.
     */
    @Override
    protected void setupConditions(Context context) {
        renderingConfig = context.get(Config.class).getRendering();
        renderingConfig.subscribe(RenderingConfig.INSCATTERING, this);
        requiresCondition(() -> renderingConfig.isInscattering());
        worldRenderer = context.get(WorldRenderer.class);
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        worldRenderer.requestTaskListRefresh();
    }
}
