import java.awt.Color;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GameFrame extends JFrame {
	private GamePanel p = new GamePanel();

	public GameFrame() {
		setTitle("Shooting Game");
		setContentPane(p);
		setSize(500, 500);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		p.startGame(); // width 값을 알아야 되기 때문에, 프레임 생성후 호출
	}

	class GamePanel extends JPanel {
		JLabel targetLabel;
		JLabel gunLabel;
		JLabel bulletLabel;

		public GamePanel() {
			setBackground(Color.WHITE);
			setLayout(null);
			// setSize() 의미없음, contentPane은 frame 크기에 따라 알아서 조절됨
			ImageIcon targetIcon = new ImageIcon("images/icon_target.png");

			targetLabel = new JLabel(targetIcon);
			targetLabel.setSize(targetIcon.getIconWidth(), targetIcon.getIconHeight());
			targetLabel.setLocation(0, 0); // in panel
			add(targetLabel);

			gunLabel = new JLabel();
			gunLabel.setSize(40, 20);
			gunLabel.setOpaque(true);
			gunLabel.setBackground(Color.BLACK);
			gunLabel.addMouseListener(new MouseAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {
					JLabel baseLabel = (JLabel) e.getSource();
					baseLabel.requestFocus();
				}

			});

			add(gunLabel);

			ImageIcon bulletIcon = new ImageIcon("images/icon_bullet.png");
			bulletLabel = new JLabel(bulletIcon);
			bulletLabel.setSize(bulletIcon.getIconWidth(), bulletIcon.getIconHeight());
			add(bulletLabel);

		}

		public void startGame() {
			int baseLabelX = this.getWidth() / 2 - (gunLabel.getWidth() / 2);
			int baseLabelY = this.getHeight() - (gunLabel.getHeight());

			int bulletLabelX = this.getWidth() / 2 - bulletLabel.getWidth() / 2;
			int bulletLabelY = this.getHeight() - bulletLabel.getHeight();

			gunLabel.requestFocus(); // 운영체제가 어떤거에 포커스를 맞춰야하는지 알려줌
										// Panel 생성후 호출
			gunLabel.setLocation(baseLabelX, baseLabelY); // Panel 생성후 호출
			bulletLabel.setLocation(bulletLabelX, bulletLabelY); // Panel 생성후 호출
			TargetThread targetThread = new TargetThread(targetLabel);
			targetThread.start();

			gunLabel.addKeyListener(new KeyAdapter() {
				BulletThread bulletThread = null;

				@Override
				public void keyPressed(KeyEvent e) {
					if (e.getKeyChar() == '\n') {
						if (bulletThread == null || bulletThread.isAlive() == false) {
							bulletThread = new BulletThread(bulletLabel, targetLabel, targetThread);
							bulletThread.start();
						}

					}
				}
			});
		}

		class TargetThread extends Thread {
			JLabel targetLabel;

			public TargetThread(JLabel targetLabel) {
				this.targetLabel = targetLabel;
			}

			public void run() {
				ImageIcon deathIcon = new ImageIcon("images/icon_target_die.png");
				Icon alivedIcon = targetLabel.getIcon();
				int length = 2;

				while (true) {
					int x = targetLabel.getX();
					int y = targetLabel.getY();
					x = x + length;

					int targetWidth = targetLabel.getWidth();
					int panelWidth = GamePanel.this.getWidth();

					int endX = x + targetWidth;
					int startX = x;

					if (endX >= panelWidth)
						length *= -1;
					else if (startX < 0)
						length *= -1;

					targetLabel.setLocation(x, y);
					targetLabel.getParent().repaint();
					try {
						sleep(10);
					} catch (InterruptedException e) {
						// shooted
						
						targetLabel.setIcon(deathIcon);
						targetLabel.getParent().repaint();
						try{
							sleep(1000);
						} catch (InterruptedException ee) {}
						targetLabel.setLocation(0, 0);
						targetLabel.setIcon(alivedIcon);
						targetLabel.getParent().repaint();
					}
				}
			}
		}

		class BulletThread extends Thread {
			JLabel bulletLabel;
			JLabel targetLabel;
			TargetThread targetThread;

			// 항상 매개변수를 통해서 받자
			public BulletThread(JLabel bulletLabel, JLabel targetLabel, TargetThread targetThread) {
				this.bulletLabel = bulletLabel;
				this.targetLabel = targetLabel;
				this.targetThread = targetThread;
			}

			public void run() {
				while (true) {
					if (hit()) {
						targetThread.interrupt();

						int x = bulletLabel.getX();
						int y = GamePanel.this.getHeight() - bulletLabel.getHeight();
						bulletLabel.setLocation(x, y);
						bulletLabel.getParent().repaint();

						return;
					}

					int x = bulletLabel.getX();
					int y = bulletLabel.getY();
					y = y - 1;

					if (y < 0) {
						y = GamePanel.this.getHeight() - bulletLabel.getHeight();
						bulletLabel.setLocation(x, y);
						bulletLabel.getParent().repaint();
						return;
					}

					bulletLabel.setLocation(x, y);
					bulletLabel.getParent().repaint();

					try {
						sleep(1);
					} catch (InterruptedException e) {
					}
				}
			}

			private boolean hit() {
				int bullet_startX = bulletLabel.getX();
				int bullet_endX = bulletLabel.getX() + bulletLabel.getWidth();

				int bullet_startY = bulletLabel.getY();
				int bullet_endY = bulletLabel.getY() + bulletLabel.getHeight();

				if (targetConaines(bullet_startX, bullet_startY) || targetConaines(bullet_startX, bullet_endY)
						|| targetConaines(bullet_endX, bullet_startY) || targetConaines(bullet_endX, bullet_endY))
					return true;
				return false;
			}

			// x,y를 클래스 변수로 넣지말고 매개변수로 받자!
			private boolean targetConaines(int x, int y) {
				int target_startX = targetLabel.getX();
				int target_endX = targetLabel.getX() + targetLabel.getWidth();

				int target_startY = targetLabel.getY();
				int target_endY = targetLabel.getY() + targetLabel.getHeight();

				if (x >= target_startX && x <= target_endX && y >= target_startY && y <= target_endY)
					return true;
				return false;
			}

		}

	}

	public static void main(String[] arg) {
		new GameFrame();
	}

}
