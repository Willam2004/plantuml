/* ========================================================================
 * PlantUML : a free UML diagram generator
 * ========================================================================
 *
 * (C) Copyright 2009-2020, Arnaud Roques
 *
 * Project Info:  http://plantuml.com
 * 
 * If you like this project or if you find it useful, you can support us at:
 * 
 * http://plantuml.com/patreon (only 1$ per month!)
 * http://plantuml.com/paypal
 * 
 * This file is part of PlantUML.
 *
 * PlantUML is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PlantUML distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
 * License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 *
 * Original Author:  Arnaud Roques
 *
 *
 */
package net.sourceforge.plantuml.sprite;

import java.awt.geom.Dimension2D;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import net.sourceforge.plantuml.Dimension2DDouble;
import net.sourceforge.plantuml.FileFormatOption;
import net.sourceforge.plantuml.ISkinParam;
import net.sourceforge.plantuml.ISkinSimple;
import net.sourceforge.plantuml.SkinParam;
import net.sourceforge.plantuml.UmlDiagram;
import net.sourceforge.plantuml.UmlDiagramType;
import net.sourceforge.plantuml.UseStyle;
import net.sourceforge.plantuml.WithSprite;
import net.sourceforge.plantuml.command.BlocLines;
import net.sourceforge.plantuml.command.Command;
import net.sourceforge.plantuml.command.CommandFactorySprite;
import net.sourceforge.plantuml.core.DiagramDescription;
import net.sourceforge.plantuml.core.ImageData;
import net.sourceforge.plantuml.cucadiagram.Display;
import net.sourceforge.plantuml.graphic.AbstractTextBlock;
import net.sourceforge.plantuml.graphic.FontConfiguration;
import net.sourceforge.plantuml.graphic.HorizontalAlignment;
import net.sourceforge.plantuml.graphic.StringBounder;
import net.sourceforge.plantuml.graphic.TextBlock;
import net.sourceforge.plantuml.graphic.TextBlockUtils;
import net.sourceforge.plantuml.preproc.Stdlib;
import net.sourceforge.plantuml.style.ClockwiseTopRightBottomLeft;
import net.sourceforge.plantuml.ugraphic.ImageBuilder;
import net.sourceforge.plantuml.ugraphic.ImageParameter;
import net.sourceforge.plantuml.ugraphic.UFont;
import net.sourceforge.plantuml.ugraphic.UGraphic;
import net.sourceforge.plantuml.ugraphic.UTranslate;
import net.sourceforge.plantuml.ugraphic.color.HColor;
import net.sourceforge.plantuml.ugraphic.color.HColorUtils;

public class StdlibDiagram extends UmlDiagram {

	private static final int WIDTH = 1800;
	private String name;

	public StdlibDiagram(ISkinSimple skinParam) {
		super(skinParam);
	}

	public DiagramDescription getDescription() {
		return new DiagramDescription("(Sprites)");
	}

	@Override
	public UmlDiagramType getUmlDiagramType() {
		return UmlDiagramType.HELP;
	}

	@Override
	protected ImageData exportDiagramInternal(OutputStream os, int index, FileFormatOption fileFormatOption)
			throws IOException {

		final TextBlock result = getTable();

		final double dpiFactor = 1;
		final int margin1;
		final int margin2;
		if (UseStyle.useBetaStyle()) {
			margin1 = SkinParam.zeroMargin(10);
			margin2 = SkinParam.zeroMargin(10);
		} else {
			margin1 = 10;
			margin2 = 10;
		}
		ISkinParam skinParam = getSkinParam();
		final HColor backcolor = skinParam.getBackgroundColor(false);
		final String metadata = fileFormatOption.isWithMetadata() ? getMetadata() : null;
		final ClockwiseTopRightBottomLeft margins = ClockwiseTopRightBottomLeft.margin1margin2(margin1, margin2);
		final ImageParameter imageParameter = new ImageParameter(skinParam, getAnimation(), dpiFactor, metadata,
				getWarningOrError(), margins, backcolor);
		final ImageBuilder imageBuilder = ImageBuilder.build(imageParameter);
		imageBuilder.setUDrawable(result);

		return imageBuilder.writeImageTOBEMOVED(fileFormatOption, seed(), os);
	}

	private TextBlock getTable() {
		return new AbstractTextBlock() {

			public void drawU(UGraphic ug) {
				try {
					drawInternal(ug);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			public Dimension2D calculateDimension(StringBounder stringBounder) {
				return new Dimension2DDouble(WIDTH, 4096);
			}
		};
	}

	public void setStdlibName(String name) {
		this.name = name;
	}

	private void drawInternal(UGraphic ug) throws IOException {
		double x = 0;
		double y = 0;
		double rawHeight = 0;
		final Stdlib folder = Stdlib.retrieve(name);

		final CommandFactorySprite factorySpriteCommand = new CommandFactorySprite();

		Command<WithSprite> cmd = factorySpriteCommand.createMultiLine(false);

		final List<String> all = folder.extractAllSprites();
		int nb = 0;
		for (String s : all) {
			// System.err.println("s="+s);
			final BlocLines bloc = BlocLines.fromArray(s.split("\n"));
			cmd.execute(this, bloc);
//			System.err.println("nb=" + nb);
			nb++;
		}

		for (String n : getSkinParam().getAllSpriteNames()) {
			final Sprite sprite = getSkinParam().getSprite(n);
			TextBlock blockName = Display.create(n).create(FontConfiguration.blackBlueTrue(UFont.sansSerif(14)),
					HorizontalAlignment.LEFT, getSkinParam());
			TextBlock tb = sprite.asTextBlock(HColorUtils.BLACK, 1.0);
			tb = TextBlockUtils.mergeTB(tb, blockName, HorizontalAlignment.CENTER);
			tb.drawU(ug.apply(new UTranslate(x, y)));
			final Dimension2D dim = tb.calculateDimension(ug.getStringBounder());
			rawHeight = Math.max(rawHeight, dim.getHeight());
			x += dim.getWidth();
			x += 30;
			if (x > WIDTH) {
				x = 0;
				y += rawHeight + 50;
				rawHeight = 0;
				if (y > 1024) {
//					break;
				}
			}
		}
	}
}
