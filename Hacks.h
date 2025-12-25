#ifndef JESSE_DX_IMPORTANT_HACKS_H
#define JESSE_DX_IMPORTANT_HACKS_H

#include "backend/Login.h"
#include "import.h"
#include "socket.h"
#include "Coloring.h"
#include "Engine.h"
#include "items.h"
#include <chrono>
#include <cstring>
using namespace std::chrono;


Request request;
Response response;

float h, w, x, y, z, yPosName, yTeamIDpos, down, CrossSize;
float sts, magic_number, mx, my, top, bottom, textsize, border;

char extra[30];
int totalPlayers = response.PlayerCount;
int botCount, playerCount, i;
Options options { 1,-1, 2,false,1,false,200,200,50,71,false,300,false,18,20,660,1400,-1,50, false , 0.1f };
Memory memory { false, false, false, false, false };
Color clr, clrHealth, clrTeamID;

/*
void DrawCenterArrow(
    ESP &esp,
    const Vec2 &worldDelta,
    float radius,
    Color clr,
    int screenWidth,
    int screenHeight
) {
    Vec2 dir = worldDelta.Normalized();
    Vec2 center(screenWidth * 0.5f, screenHeight * 0.5f);

    // selalu tempel di boundary lingkaran
    Vec2 tip = center + dir * radius;

    float arrowSize = 30.0f;

    Vec2 left  = tip - dir.Perp() * (arrowSize * 0.5f) - dir * arrowSize;
    Vec2 right = tip + dir.Perp() * (arrowSize * 0.5f) - dir * arrowSize;

    // stroke hitam
    esp.DrawTriangle(Color(0, 0, 0, 255), tip, left, right, 4.0f);

    // isi
    esp.DrawTriangleFilled(clr, tip, left, right);

    // outline tipis
    esp.DrawTriangle(clr, tip, left, right, 1.5f);
}*/

                      ////================= isRadar ================////

void DrawRadar(ESP canvas, Vec2 Location, Vec2 Pos, float Size, Color clr) {
    // LocalPos
    canvas.DrawFilledRect(Color(255, 255, 255), {Pos.x - Size / 25, Pos.y - Size / 25}, {Pos.x + Size / 25, Pos.y + Size / 25});
    
    // EnemyPos
    canvas.DrawFillCircle(Color(clr.r, clr.g, clr.b, 255), Location, Size / 20, 0.5);
    
    
}

void DrawESP(ESP esp, int screenWidth, int screenHeight) {

  if (!bValid && !lolo)
    return;
  if (!g_Token.empty()) {
    if (!g_Auth.empty()) {
      if (g_Token == g_Auth) {
	  
        esp.DrawTextMode(Color(255, 255, 255, 255), "",
                         Vec2(screenWidth / 5, screenHeight / 1.04),
                         screenHeight / 45);
       
	
        botCount = 0, playerCount = 0;
        request.ScreenHeight = screenHeight;
        request.ScreenWidth = screenWidth;
        request.Mode = InitMode;
        request.options = options;
        request.memory = memory;
        send((void *)&request, sizeof(request));
        receive((void *)&response);
        request.radarPos = {screenWidth / 4, screenHeight / 4};

        ////================= isCrosshair ================////
        
        if (Crosshair) {
          esp.DrawCrosshair(Color(255, 0, 0, 255),
                            Vec2(screenWidth / 2, screenHeight / 2), 42);
	}
	
	//Fps
	if (ShowFps) {
	  esp.DrawTextName(Color(0, 255, 19), "",
                            Vec2(screenWidth / 10.3, 70), screenHeight / 48);
			    }
			    
        char hello[50] = "failed";
        if (response.Success) {
	  esp.DrawFillCircle(Color(0, 0, 0, 35), request.radarPos, request.radarSize + (request.radarSize / 10), 0.5);
          esp.DrawCircle(Color(255, 255, 255, 200), request.radarPos, request.radarSize + (request.radarSize / 10), 1);
	

          float textsize = screenHeight / 50;
          Vec2 screen(screenWidth, screenHeight);
          float mScale = screenHeight / (float)1080;
          
          for (int i = 0; i < response.PlayerCount; i++) {
	  PlayerData Player = response.Players[i];
		
            x = Player.HeadLocation.x;
            y = Player.HeadLocation.y;
            h = Player.Bone.root.x;
            w = Player.Bone.root.y;

	    sprintf(extra, "%0.0fM", Player.Distance);
            float magic_number = (Player.Distance * response.fov);
            float mx = (screenWidth / 4) / magic_number;
            float my = (screenWidth / 1.38) / magic_number;
            float top = y - my + (screenWidth / 1.7) / magic_number;
            float bottom = Player.Bone.lAn.y + my - (screenWidth / 1.7) / magic_number;
            Color _colorByDistance = colorByDistance((int)Player.Distance, 255);
            yPosName = top - screenHeight / 30;
            float namewidht = (screenWidth / 6) / magic_number;
	    float pp2 = namewidht / 2;
	    //Vec2 location(x, y); //
	    
             if (Player.isBot) {
		 if (isPlayerNotBot) {
              botCount++;
              clr = Color(255, 255, 255, 255);
	      }
            } else {
              playerCount++;
              if (Player.Distance < 125) {
                clr = Color(255, 0, 0, 255);
              } else if (Player.Distance < 175) {
                clr = Color(237, 165, 0, 255);
              } else {
                clr = Color(237, 202, 5, 255);
              }
            }
	  
	  DrawRadar(esp, Player.RadarLocation, request.radarPos, request.radarSize, clr);
	  
	  /*
	  bool playerInCenter = colorPosCenter(screenWidth / 2, x - mx, screenHeight / 2,
                                     top, screenWidth / 2, x + mx, screenHeight / 2, bottom);
	  clr   = playerInCenter ? Color::Green()  : clr;
	  */
	  
	    if (isnobot) {
                isPlayerNotBot = !response.Players[i].isBot;
            } else {
                isPlayerNotBot = true;
            }

            if (Player.HeadLocation.z != 1 && isPlayerNotBot) {

              if (x > -50 && x < screenWidth + 50) {
                  
                      ////================= isBone ================////

                if (Bone && Player.Bone.isBone) {
		    
                  esp.DrawLinePlayer(clr, 1.2, Vec2(x, y),
                                  Vec2(Player.Bone.neck.x,
                                       Player.Bone.neck.y));
                  esp.DrawLinePlayer(clr, 1.2,
                                  Vec2(Player.Bone.neck.x,
                                       Player.Bone.neck.y),
                                  Vec2(Player.Bone.cheast.x,
                                       Player.Bone.cheast.y));
                  esp.DrawLinePlayer(clr, 1.2,
                                  Vec2(Player.Bone.cheast.x,
                                       Player.Bone.cheast.y),
                                  Vec2(Player.Bone.pelvis.x,
                                       Player.Bone.pelvis.y));
                  esp.DrawLinePlayer(clr, 1.2,
                                  Vec2(Player.Bone.neck.x,
                                       Player.Bone.neck.y),
                                  Vec2(Player.Bone.lSh.x,
                                       Player.Bone.lSh.y));
                  esp.DrawLinePlayer(clr, 1.2,
                                  Vec2(Player.Bone.neck.x,
                                       Player.Bone.neck.y),
                                  Vec2(Player.Bone.rSh.x,
                                       Player.Bone.rSh.y));
                  esp.DrawLinePlayer(clr, 1.2,
                                  Vec2(Player.Bone.lSh.x,
                                       Player.Bone.lSh.y),
                                  Vec2(Player.Bone.lElb.x,
                                       Player.Bone.lElb.y));
                  esp.DrawLinePlayer(clr, 1.2,
                                  Vec2(Player.Bone.rSh.x,
                                       Player.Bone.rSh.y),
                                  Vec2(Player.Bone.rElb.x,
                                       Player.Bone.rElb.y));
                  esp.DrawLinePlayer(clr, 1.2,
                                  Vec2(Player.Bone.lElb.x,
                                       Player.Bone.lElb.y),
                                  Vec2(Player.Bone.lWr.x,
                                       Player.Bone.lWr.y));
                  esp.DrawLinePlayer(clr, 1.2,
                                  Vec2(Player.Bone.rElb.x,
                                       Player.Bone.rElb.y),
                                  Vec2(Player.Bone.rWr.x,
                                       Player.Bone.rWr.y));
                  esp.DrawLinePlayer(clr, 1.2,
                                  Vec2(Player.Bone.pelvis.x,
                                       Player.Bone.pelvis.y),
                                  Vec2(Player.Bone.lTh.x,
                                       Player.Bone.lTh.y));
                  esp.DrawLinePlayer(clr, 1.2,
                                  Vec2(Player.Bone.pelvis.x,
                                       Player.Bone.pelvis.y),
                                  Vec2(Player.Bone.rTh.x,
                                       Player.Bone.rTh.y));
                  esp.DrawLinePlayer(clr, 1.2,
                                  Vec2(Player.Bone.lTh.x,
                                       Player.Bone.lTh.y),
                                  Vec2(Player.Bone.lKn.x,
                                       Player.Bone.lKn.y));
                  esp.DrawLinePlayer(clr, 1.2,
                                  Vec2(Player.Bone.rTh.x,
                                       Player.Bone.rTh.y),
                                  Vec2(Player.Bone.rKn.x,
                                       Player.Bone.rKn.y));
                  esp.DrawLinePlayer(clr, 1.2,
                                  Vec2(Player.Bone.lKn.x,
                                       Player.Bone.lKn.y),
                                  Vec2(Player.Bone.lAn.x,
                                       Player.Bone.lAn.y));
                  esp.DrawLinePlayer(clr, 1.2,
                                  Vec2(Player.Bone.rKn.x,
                                       Player.Bone.rKn.y),
                                  Vec2(Player.Bone.rAn.x,
                                       Player.Bone.rAn.y));
		 esp.DrawCircle(clr,
                                 Vec2(Player.HeadLocation.x,
                                      Player.HeadLocation.y - 3),
                                 screenHeight / 8 / magic_number, 2);
                }

                ////================= isPlayerBox ================////
                
                if (Box) {
                    esp.DrawRect(clr, 1.3, Vec2(x - namewidht, top),
                               Vec2(x + namewidht, bottom));
		   }

                ////================= isTopLine ================////
                
		if (Line) {
    if (Player.Health != 0) { // Jika tidak knock
        esp.DrawLinePlayer(
            clr, 1.4, Vec2(screenWidth / 2, screenHeight / 35),
            Vec2(x, top - screenHeight / 52));
    }
}

                ////================= isPlayerHealth ================////
             
if (Health) {              
    int lhealth = 30; // Default value if neither HealthV1 nor HealthV2 is true
    
    if (HealthV1) {
        lhealth = 13;
    } else if (HealthV2) {
        lhealth = 30;
    }
    
    // Use consistent player reference - choose either response.Players[i] or Player
    float currentHealth = response.Players[i].Health;
    
    // Determine health color based on current health value
    Color healthColor;
    if (currentHealth < 25) {
        if (HealthV1) {
            healthColor = Color(255, 0, 0, 150);
        } else {
            healthColor = Color(255, 0, 0);
        }
    } else if (currentHealth < 50) {
        if (HealthV1) {
            healthColor = Color(255, 165, 0, 150);
        } else {
            healthColor = Color(255, 165, 0);
        }
    } else if (currentHealth < 75) {
        if (HealthV1) {
            healthColor = Color(255, 255, 0, 150);
        } else {
            healthColor = Color(255, 255, 0);
        }
    } else {
        if (HealthV1) {
            healthColor = Color(0, 120, 0, 150);
        } else {
            healthColor = Color(0, 255, 0);
        }
    }
    
    // Final color selection based on HealthTimID setting
    Color finalColor;
    if (HealthTimID) {
        // Use team ID color with appropriate alpha
        float alpha = HealthV1 ? 150.0f : 150.0f;
        finalColor = _clrTeamID(Player.TeamID, alpha);
    } else {
        // Use health-based color
        finalColor = healthColor;
    }
    
    float healthLength = screenHeight / lhealth;
    if (healthLength < mx) {
        healthLength = mx;
    }
    
    if (Player.Health == 0) {
        esp.DrawJess(Color(255,255,255,255), "KNOCK OUT",
            Vec2(x, top - screenHeight / 28), textsize - 3);
        esp.DrawJess(Color(255,255,255,255), "▼",
            Vec2(x, top - screenHeight / 56), textsize - 3);
    } else {
        if (HealthV1) {
            esp.DrawFilledRect(
                finalColor, // Use the final selected color
                Vec2(x - healthLength, top - screenHeight / 30),
                Vec2(x - healthLength +
                        (2 * healthLength) *
                        Player.Health / 100,
                    top - screenHeight / 110));
            esp.DrawRect(Color(0,0,0), 1,
                Vec2(x - healthLength, top - screenHeight / 30),
                Vec2(x + healthLength, top - screenHeight / 110));
        } else if (HealthV2) {
            esp.DrawFilledRect(finalColor, // Use the final selected color
                Vec2(x - healthLength, top - screenHeight / 110), 
                Vec2(x - healthLength + (2 * healthLength) * currentHealth / 100,
                top - screenHeight / 225));
            esp.DrawRect(Color(0, 0, 0), 1.5,
                Vec2(x - healthLength, top - screenHeight / 110), 
                Vec2(x + healthLength, top - screenHeight / 225));
        }
    }
}
		if (Status)
                  {
                      const auto &state = Player.States;
                      esp.DrawStates(Color(0,255,255,255), state, Vec2(x, top - screenHeight / 10),
                                 textsize - 3);
                  }
		  
		if (PlayerUID) {

                      esp.DrawUserID(Color(255, 255, 255, 255), Player.PlayerUID, Vec2(x, top - screenHeight / 13),
                                 textsize - 3);
                  }
		
		if (Name) { // Jika tidak knock
		    if (Player.Health == 0) {
        		esp.DrawJess(Color(255,255,255,255), "KNOCK OUT",
            			Vec2(x, top - screenHeight / 28), textsize - 3);
        		esp.DrawJess(Color(255,255,255,255), "▼",
            			Vec2(x, top - screenHeight / 56), textsize - 3);
    		} else {
			
                    esp.DrawName2(Color(255, 255, 255, 255), Player.PlayerNameByte,
                          Player.TeamID,
                          Vec2(x, top - screenHeight / 65), textsize - 3);
                 } //else
	      } //name
	   
                ////================= isPlayerDist ================////
                if (Distance) {

                    esp.DrawJess(Color(255, 255, 255, 255), extra,
                                 Vec2(x, bottom + screenHeight / 50), textsize);
                 
                }

                if (EnemyWeapon && Player.Weapon.isWeapon) {
                  esp.DrawWeapon(Color(255, 255, 255, 255), Player.Weapon.id,
                                 Player.Weapon.ammo, 
				 Vec2(x, top - screenHeight / 20),
                                 textsize - 3);
              }
	      
	      
           }
	} //
            
            ////================= isBackAlert ================////
	    
	  Vec2 location(response.Players[i].HeadLocation.x, response.Players[i].HeadLocation.y);

if (BackmarkAlert) {
    if (AlertV1) {
        // ========== UI 1 : Dot + Text ==========
        if (isOutsideSafeZone(location, screen)) {
            Vec2 hintDotRenderPos = pushToScreenBorder(location, screen, 0);
            Vec2 hintTextRenderPos = pushToScreenBorder(location, screen, -(int)(mScale * 30));

            esp.DrawFilledCircle(_colorByDistance, hintDotRenderPos, (mScale * 60));

            char extra[64];
            sprintf(extra, "%0.0fm", response.Players[i].Distance);
            esp.DrawText(Color(255, 255, 255), extra, hintTextRenderPos, textsize);
        }
	
    } else if (AlertV2) {
        /*// ========== UI 2 : Arrow ==========
	float arrowRadius = 180.0f;
	// cari center screen
	Vec2 center(screenWidth * 0.5f, screenHeight * 0.5f);

	// delta relatif ke center
	Vec2 delta(Player.HeadLocation.x - center.x,
           Player.HeadLocation.y - center.y);
	   float dist = delta.Length();

    	Color arrowClr = colorByDistance(response.Players[i].Distance, 200.0f);
    	DrawCenterArrow(esp, delta, arrowRadius, arrowClr, screenWidth, screenHeight);
	
	
	    }*/
	    
	    if (isOutsideSafeZone(location, screen)) {
                                    // Triangle ko screen ke border pe push karo
                                    Vec2 hintDotRenderPos = pushToScreenBorder2(location, screen,
                                                                               (mScale * 100) / 2,
                                                                               5.0f);


                                    float angle = getDisplayAngle(hintDotRenderPos, screen);

                                    if (response.Players[i].isBot) {
                                        esp.DrawTriangle2(Color(255,255,255,255), hintDotRenderPos,
                                                         (mScale * 20), angle);
                                    } else {
                                        esp.DrawTriangle2(Color(255,0,0,255), hintDotRenderPos,
                                                         (mScale * 20), angle);
                                    }
                                }
			}
	}
    }
    
		for (int i = 0; i < response.GrenadeCount; i++) {
                if (!GrenadeWarning)
                    continue;
                if (response.Grenade[i].Location.z != 1.0f) {
                    if (response.Grenade[i].type == 1) {
                        sprintf(extra, "Grenade (%0.0f m)",
                                response.Grenade[i].Distance);
                        esp.DrawText(Color(255, 0, 0, 255), extra,
                                     Vec2(response.Grenade[i].Location.x,
                                          response.Grenade[i].Location.y + 20), textsize);
                    } else if (response.Grenade[i].type == 2) {
                        sprintf(extra, "Molotov (%0.0f m)",
                                response.Grenade[i].Distance);
                        esp.DrawText(Color(255, 169, 0, 255), extra,
                                     Vec2(response.Grenade[i].Location.x,
                                          response.Grenade[i].Location.y + 20), textsize);
                    } else if (response.Grenade[i].type == 3) {
                        sprintf(extra, "Stun (%0.0f m)",
                                response.Grenade[i].Distance);
                        esp.DrawText(Color(255, 255, 0, 255), extra,
                                     Vec2(response.Grenade[i].Location.x,
                                          response.Grenade[i].Location.y + 20), textsize);
                    } else if (response.Grenade[i].type == 4) {
                        sprintf(extra, "Smoke (%0.0f m)",
                                response.Grenade[i].Distance);
                        esp.DrawText(Color(0, 255, 0, 255), extra,
                                     Vec2(response.Grenade[i].Location.x,
                                          response.Grenade[i].Location.y + 20), textsize);
                    }  else {
                        sprintf(extra, "AirAttackBomb (%0.0f m)",
                                response.Grenade[i].Distance);
                        esp.DrawText(Color(255, 0, 0, 255), extra,
                                     Vec2(response.Grenade[i].Location.x,
                                          response.Grenade[i].Location.y + 20), textsize);
                    }
                }
		
                int WARNING = 4;
                if (!response.Grenade[i].type < 5 && response.Grenade[i].type == 5) {
                    esp.DrawJess(Color(255, 0, 0, 255), "Warning! - Redzone ️",
                         Vec2(screenWidth / 2, screenHeight / 5), textsize + 7);
                } else {
                        char extra[64];
			snprintf(extra, sizeof(extra), "Warning! (%d) Throwables", response.GrenadeCount);
			esp.DrawJess(Color(255, 0, 0, 255), extra, Vec2(screenWidth / 2, screenHeight / 3), textsize + 7);
                }

                if (response.Grenade[i].Location.z != 1.0f) {
                    if (response.Grenade[i].type == 1)
                        esp.DrawText(Color(255, 0, 0, 255), "〇",
                                     Vec2(response.Grenade[i].Location.x,
                                          response.Grenade[i].Location.y), textsize);
                    else if (response.Grenade[i].type == 2)
                        esp.DrawText(Color(255, 169, 0, 255), "〇",
                                     Vec2(response.Grenade[i].Location.x,
                                          response.Grenade[i].Location.y), textsize);
                    else if (response.Grenade[i].type == 3)
                        esp.DrawText(Color(255, 255, 0, 255), "〇",
                                     Vec2(response.Grenade[i].Location.x,
                                          response.Grenade[i].Location.y), textsize);
                    else if (response.Grenade[i].type == 4)
                        esp.DrawText(Color(0, 255, 0, 255), "〇",
                                     Vec2(response.Grenade[i].Location.x,
                                          response.Grenade[i].Location.y), textsize);
                    else
                        esp.DrawText(Color(255, 0, 0, 255), "〇",
                                     Vec2(response.Grenade[i].Location.x,
                                          response.Grenade[i].Location.y), textsize);
                }
            }

        ////================= isItems ================////
        if (Items) {
          for (int i = 0; i < response.ItemsCount; i++) {
            if (IsValidLocation(response.Items[i].Location)) {
              esp.DrawItems(response.Items[i].ItemName,
                            response.Items[i].Distance,
                            Vec2(response.Items[i].Location.x,
                                 response.Items[i].Location.y),
                            screenHeight / 50);
            }
          }
        }
      
          ////================= isVehicle ================////
        if (Vehicle) {
            for (int i = 0; i < response.VehicleCount; i++) {
              if (IsValidLocation(response.Vehicles[i].Location)) {
                esp.DrawVehicles(response.Vehicles[i].VehicleName,
                                 response.Vehicles[i].Distance,
                                 response.Vehicles[i].Health,
                                 response.Vehicles[i].Fuel,
                                 Vec2(response.Vehicles[i].Location.x,
                                      response.Vehicles[i].Location.y),
                                 textsize);
              }
            }
          }
	  
	  
      ////================= isAntiBokong ================////
      
if (NearAlert) {
    if (playerCount > 0 || botCount > 0) { // pastikan ada musuh
        if (response.Players[i].HeadLocation.z != 1.0f) {
            if (response.Players[i].Distance < 60) {
                esp.DrawJess(
                    Color(255, 224, 0, 255), 
                    "⚠️ DANGER ENEMY[s] NEARBY️️",
                    Vec2(screenWidth / 2, screenHeight / 4), 
                    textsize + 7
                );
            }
        }
    }
}

}

if (CountV3) {
    float scale = 0.6f;

    // === BASE UNIT (IKUT HEIGHT, BUKAN WIDTH) ===
    float baseH = screenHeight;

    float top = baseH / 15 * scale;
    float height = baseH / 9 * scale;

    // Width diturunkan dari height (INI KUNCI STABILITAS)
    float width = height * 2.2f;   // rasio aman (boleh 2.0 – 2.5)
    float shrink = height * 0.9f;

    float fontSize = height * 0.55f;
    float lineThickness = height * 0.08f;
    float radius = height / 2;

    Vec2 center(screenWidth / 2, top);

    // Warna (TIDAK DIUBAH)
    Color textLeft(0, 255, 0);
    Color textRight(244, 98, 88);
    Color bg(0, 0, 0, 125);
    Color clearColor(0, 0, 0, 125);

    // Spacing ikut height juga
    float countSpacing = height * 0.45f;

    if (botCount + playerCount > 0) {
        esp.DrawFilledRoundedRect(clearColor,
            Vec2(center.x - (width - shrink), top),
            Vec2(center.x + (width - shrink), top + height),
            radius);

        esp.DrawRoundedRect(Color(97,97,97),
            lineThickness,
            Vec2(center.x - (width - shrink), top),
            Vec2(center.x + (width - shrink), top + height),
            radius);

        sprintf(extra, "%d", botCount);
        esp.DrawTextCount(textLeft, extra,
            Vec2(center.x - countSpacing, top + height / 2 + fontSize / 3),
            fontSize);

        sprintf(extra, "%d", playerCount);
        esp.DrawTextCount(textRight, extra,
            Vec2(center.x + countSpacing, top + height / 2 + fontSize / 3),
            fontSize);

    } else {
        esp.DrawFilledRoundedRect(clearColor,
            Vec2(center.x - (width - shrink), top),
            Vec2(center.x + (width - shrink), top + height),
            radius);

        esp.DrawRoundedRect(Color(97,97,97),
            lineThickness,
            Vec2(center.x - (width - shrink), top),
            Vec2(center.x + (width - shrink), top + height),
            radius);

        const char* lobbyText = response.InLobby ? "Lobby" : "Clear";
        esp.DrawTextCount(Color(0,255,0), lobbyText,
            Vec2(center.x, top + height / 2 + fontSize / 3),
            fontSize);
    }
}

    ////================= isCount2 ================////
    
    if (CountV2) {
bool isSafe = (playerCount == 0 && botCount == 0);

if (!isSafe) {
    if (playerCount > 0) {
        // Player Count box
        esp.DrawEnemyCount(Color(0, 0, 0, 30),
                           Vec2(screenWidth / 2 - screenHeight / 14, screenHeight / 15),
                           Vec2(screenWidth / 2, screenHeight / 8));
        esp.DrawEnemyCount(Color(255, 0, 0, 30),
                           Vec2(screenWidth / 2 - screenHeight / 14, screenHeight / 15),
                           Vec2(screenWidth / 2, screenHeight / 8));
        sprintf(extra, "%d", playerCount);
        esp.DrawJess(Color(255, 0, 0, 255), extra,
                          Vec2(screenWidth / 2 - screenHeight / 28, screenHeight / 8.85),
                          screenHeight / 22);
    }

    if (botCount > 0) {
        // Bot Count box
        esp.DrawEnemyCount2(Color(0, 0, 0, 30),
                            Vec2(screenWidth / 2, screenHeight / 15),
                            Vec2(screenWidth / 2 + screenHeight / 14, screenHeight / 8));
        esp.DrawEnemyCount2(Color(255, 255, 255, 30),
                            Vec2(screenWidth / 2, screenHeight / 15),
                            Vec2(screenWidth / 2 + screenHeight / 14, screenHeight / 8));
        sprintf(extra, "%d", botCount);
        esp.DrawJess(Color(255, 255, 255, 255), extra,
                          Vec2(screenWidth / 2 + screenHeight / 28, screenHeight / 8.85),
                          screenHeight / 22);
    }

    // Draw zero if count = 0
    if (playerCount == 0) {
        esp.DrawEnemyCount(Color(0, 0, 0, 30),
                           Vec2(screenWidth / 2 - screenHeight / 14, screenHeight / 15),
                           Vec2(screenWidth / 2, screenHeight / 8));
        esp.DrawEnemyCount(Color(40, 252, 80, 30),
                           Vec2(screenWidth / 2 - screenHeight / 14, screenHeight / 15),
                           Vec2(screenWidth / 2, screenHeight / 8));
        esp.DrawJess(Color(40, 252, 80), "0",
                          Vec2(screenWidth / 2 - screenHeight / 28, screenHeight / 8.85),
                          screenHeight / 22);
    }

    if (botCount == 0) {
        esp.DrawEnemyCount2(Color(0, 0, 0, 30),
                            Vec2(screenWidth / 2, screenHeight / 15),
                            Vec2(screenWidth / 2 + screenHeight / 14, screenHeight / 8));
        esp.DrawEnemyCount2(Color(40, 252, 80, 30),
                            Vec2(screenWidth / 2, screenHeight / 15),
                            Vec2(screenWidth / 2 + screenHeight / 14, screenHeight / 8));
        esp.DrawJess(Color(40, 252, 80, 255), "0",
                          Vec2(screenWidth / 2 + screenHeight / 28, screenHeight / 8.85),
                          screenHeight / 22);
    }

} else {
    // SAFE MODE — No enemy & bot
    Vec2 leftBoxStart = Vec2(screenWidth / 2 - screenHeight / 14, screenHeight / 15);
    Vec2 rightBoxEnd = Vec2(screenWidth / 2 + screenHeight / 14, screenHeight / 8);

    // Draw merged box background (same as two DrawEnemyCount boxes)
    esp.DrawEnemyCount(Color(0, 0, 0, 30), leftBoxStart, rightBoxEnd);
    esp.DrawEnemyCount2(Color(40, 252, 80, 30), leftBoxStart, rightBoxEnd);

    // Draw SAFE Text
    const char* lobbyText = response.InLobby ? "Lobby" : "Clear";
    esp.DrawJess(Color(40, 252, 80, 255), lobbyText,
                 Vec2(screenWidth / 2 - screenHeight / 350, screenHeight / 8.85),
                 screenHeight / 22);
}
}

    ////================= isCount1 ================////
    
    if (CountV1) {
      int ENEM_ICON = 2;
          int BOT_ICON = 3;

          if (playerCount == 0) {
            ENEM_ICON = 0;
          }
          if (botCount == 0) {
            BOT_ICON = 1;
          }

          char cn[10];
          sprintf(cn, "%d", playerCount);

          char bt[10];
          sprintf(bt, "%d", botCount);

      esp.DrawOTH1(Vec2(screenWidth / 2 - (80), 60), ENEM_ICON);
      esp.DrawOTH1(Vec2(screenWidth / 2, 60), BOT_ICON);
      esp.DrawJess(Color(255, 255, 255, 255), cn,
                   Vec2(screenWidth / 2 - (25), 87), 23);
      esp.DrawJess(Color(255, 255, 255, 255), bt,
                   Vec2(screenWidth / 2 + (25), 87), 23);
    }
  
  if (showtouchposition) {
    int Posix = request.options.touchX;
    esp.DrawFilledCircle(Color(255, 0, 0,150),
                     Vec2(request.options.touchY,
                     screenHeight - Posix),
		     40
		     );
    
}
  
  ////================= FOV Circle ================////

if (Fov) {
    // Pre-calculate common values once
    const Vec2 screenCenter(screenWidth / 2, screenHeight / 2);
    const float circleThickness = screenHeight / 800;
    const Vec2 textPosition(screenWidth / 5, screenHeight / 1.07);
    const float textSize = screenHeight / 45;
    const Color white(255, 255, 255, 255);

    if (options.openState == 0) {
        // Aimtouch Simulation Mode
        esp.DrawCircle(white, screenCenter, request.options.aimingRange, circleThickness);
        esp.DrawJess(white, "Aimtouch Simulation", textPosition, textSize);

    } else if (options.aimBullet == 0) {  
        // Bullet Tracking Mode
        esp.DrawCircle(Color(255, 0, 0, 255), screenCenter, 
                      request.options.aimingRangeBt, circleThickness);  
        esp.DrawJess(white, "Bullet Tracking", textPosition, textSize);
    }
}

   	    } //g_Token == g_Auth
        } //g_Auth
    } //g_Token
} //OnDraw

#endif // JESSE_DX_ENDIF_DESI

/*
if (Player.Health == 0) {
                      
                    } else {
		    
                      esp.DrawFilledRect(
                          clrHealth,
                          Vec2(x - healthLength, top - screenHeight / 30),
                          Vec2(x - healthLength +
                                   (2 * healthLength) *
                                       Player.Health / 100,
                               top - screenHeight / 110));
                      esp.DrawRect(Color(0,0,0), 1,
                          Vec2(x - healthLength, top - screenHeight / 30),
                          Vec2(x + healthLength, top - screenHeight / 110));
                    }
                    
                    ////================= isH2 ================////

                  } else if (HealthV2) { //Bsilent
                    float healthLength =
                        screenWidth / 100;
                    if (healthLength < mx)
                      healthLength = mx;
		      
                    if (Player.Health == 0) {

                    } else {
		    
                      esp.DrawFilledRect(Color(255,0,0),
                          Vec2(x - healthLength, top - screenHeight / 100),
                          Vec2(x + healthLength, top - screenHeight / 200));

                      esp.DrawFilledRect(Color(0,255,0),
                          Vec2(x - healthLength, top - screenHeight / 100),
                          Vec2(x - healthLength +
                                   (2 * healthLength) *
                                       Player.Health / 100,
                               top - screenHeight / 200));
                    }
                  }
                }
		
		if (Status)
                  {
                      const auto &state = Player.States;
                      esp.DrawStates(Color(0,255,255), state, Vec2(x, top - screenHeight / 13),
                                 textsize - 3);
                  }
		
		if (Name && Player.Health != 0) { // Jika tidak knock
                  if (Player.isBot) {
                      
                    ////================= isBot Name ================////
                    
                    if (HealthV1) {
			
			esp.DrawText(Color(255, 255, 255), "Bot",
                                   Vec2(x, top - screenHeight / 65),
                                   textsize - 3);
				   
				       
                    } else if (HealthV2) {
			
                      esp.DrawJess(Color(255, 255, 255), "Bot",
                                   Vec2(x, bottom + screenHeight / 50),
                                   textsize - 3);
				   
				   }
				   
                  } else {
                      
                       ////================= isH1 Name ================////
                    
                    if (HealthV1) { //standart
			
                      esp.DrawName2(Color(255, 255, 255), Player.PlayerNameByte,
                          Player.TeamID,
                          Vec2(x, top - screenHeight / 65), textsize - 3);
			  
                          
                      ////================= isH2 Name ================////
                      
                    } else if (HealthV2) { //bsilent
			
                      esp.DrawName3(Color(255, 255, 255), Player.PlayerNameByte,
                          Player.TeamID,
                          Vec2(x, bottom + screenHeight / 50), textsize - 3);
                    
                  } 
                }
	      }
	      */


