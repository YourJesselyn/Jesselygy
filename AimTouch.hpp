#include <unistd.h>
#include <cstdlib>
#include <fcntl.h>
#include <dirent.h>
#include <pthread.h>
#include <fstream>
#include <cstring>
#include <ctime>
#include <malloc.h>
#include <iostream>
#include <fstream>
#include <sys/system_properties.h>
#include <ctime>
#include <random>
#include <chrono>
#include <thread>
#include "TouchInput.hpp"

float ScrWidth,ScrHeight;
bool isFiring;
bool aimTouch;
float aimSmooth = 18.f;
float aim_x,aim_y;
bool isAim = false;
bool ChargingPortLeft = false;
float aimingSpeed = 20.f;
float touchRange = 200.f;
float touchRecoilCompensation;
float touchX = 650.f;
float touchY = 1400.f;
float addCompensation;
CameraView MinimalViewInfo{};

bool isDown = false;

float GetPitch(float Fov) {
    if ((int) Fov == (float) 80)
    {
        return (0.7f / 30) * touchRecoilCompensation;
    } else if ((int) Fov == (float) 70 || (int) Fov == (float) 75)
    {
        return (2.35f / 30) * touchRecoilCompensation;
    } else if ((int) Fov == (float) 55 || (int) Fov == (float) 60)
    {
        return (2.75f / 30) * touchRecoilCompensation;
    } else if ((int) Fov == (float) 44)
    {
        return (4.65f / 30) * touchRecoilCompensation;
    } else if ((int) Fov == (float) 26)
    {
        return (7.2f / 30) * touchRecoilCompensation;
    } else if ((int) Fov == (float) 20)
    {
        return (8.6f / 30) * touchRecoilCompensation;
    } else if ((int) Fov == (float) 13)
    {
        return (14.3f / 30) * touchRecoilCompensation;
    }
    return (2.5f / 30) * touchRecoilCompensation;
}

[[noreturn]] void *AimBotAuto(void *) {
    //LOGI("Thread Called");
    sleep(5);
    double tx = touchX, ty = touchY;
    float ScreenX = ScrHeight, ScreenY = ScrWidth;
    float TargetX = 0;
    float TargetY = 0;
    float zm_x = 0, zm_y = 0;
    float ScrXH = ScrHeight / 2;
    float ScrYH = ScrWidth / 2;
    float py = ScrHeight / 2;
    float px = ScrWidth / 2;
    TouchInput::touchInputStart(ScrHeight, ScrWidth);
	//LOGI("AimTouch.hpp ScrWidth %f, ScrHeight %f", ScrWidth, ScrHeight);
    while (true) {
        ScreenX = ScrHeight, ScreenY = ScrWidth;
        if (!aimTouch) {
            isAim = false;
        }
        addCompensation = GetPitch(MinimalViewInfo.FOV);
        float AimDs = sqrt(pow(px - aim_x, 2) + pow(py - aim_y, 2));
        zm_y = aim_x;
        zm_x = ScreenX - aim_y;
        if (zm_x <= 0 || zm_x >= ScreenX || zm_y <= 0 || zm_y >= ScreenY) {
            isAim = false;
        }
        //LOGI("ScrX %f, ScrY %f, isAIM %d, zm_x %f, zm_y %f, %f, %f", ScreenX, ScreenY, isAim, zm_x, zm_y, ty, tx);
        if (isAim) {
            if (!isDown) {
                if (!ChargingPortLeft)
                    TouchInput::sendTouchMove((int) tx, (int) ty);
                else
                    TouchInput::sendTouchMove(py * 2 - (float) tx, px * 2 - (float) ty);
                isDown = true;
            }
            float Aimspeace = aimingSpeed;
            if (AimDs < 1)
                Aimspeace = aimingSpeed / 0.09;
            else if (AimDs < 2)
                Aimspeace = aimingSpeed / 0.11;
            else if (AimDs < 3)
                Aimspeace = aimingSpeed / 0.12;
            else if (AimDs < 5)
                Aimspeace = aimingSpeed / 0.15;
            else if (AimDs < 10)
                Aimspeace = aimingSpeed / 0.25;
            else if (AimDs < 15)
                Aimspeace = aimingSpeed / 0.4;
            else if (AimDs < 20)
                Aimspeace = aimingSpeed / 0.5;
            else if (AimDs < 25)
                Aimspeace = aimingSpeed / 0.6;
            else if (AimDs < 30)
                Aimspeace = aimingSpeed / 0.7;
            else if (AimDs < 40)
                Aimspeace = aimingSpeed / 0.75;
            else if (AimDs < 50)
                Aimspeace = aimingSpeed / 0.8;
            else if (AimDs < 60)
                Aimspeace = aimingSpeed / 0.85;
            else if (AimDs < 70)
                Aimspeace = aimingSpeed / 0.9;
            else if (AimDs < 80)
                Aimspeace = aimingSpeed / 0.95;
            else if (AimDs < 90)
                Aimspeace = aimingSpeed / 1.0;
            else if (AimDs < 100)
                Aimspeace = aimingSpeed / 1.05;
            else if (AimDs < 150)
                Aimspeace = aimingSpeed / 1.25;
            else if (AimDs < 200)
                Aimspeace = aimingSpeed / 1.5;
            else
                Aimspeace = aimingSpeed / 1.55;

            //LOGI("AimSpeace : %f", Aimspeace);

            if (zm_x > ScrXH) {
                TargetX = -(ScrXH - zm_x);
                TargetX /= Aimspeace;
                if (TargetX + ScrXH > ScrXH * 2)
                    TargetX = 0;
            }
            if (zm_x < ScrXH) {
                TargetX = zm_x - ScrXH;
                TargetX /= Aimspeace;
                if (TargetX + ScrXH < 0)
                    TargetX = 0;
            }
            if (zm_y > ScrYH) {
                TargetY = -(ScrYH - zm_y);
                TargetY /= Aimspeace;
                if (TargetY + ScrYH > ScrYH * 2)
                    TargetY = 0;
            }
            if (zm_y < ScrYH) {
                TargetY = zm_y - ScrYH;
                TargetY /= Aimspeace;
                if (TargetY + ScrYH < 0)
                    TargetY = 0;
            }
            if (TargetY >= 35 || TargetX >= 35 || TargetY <= -35 || TargetX <= -35) {
                if (isDown) {
                    usleep(1000);
                    tx = touchX, ty = touchY;
                    TouchInput::sendTouchUp();
                    isDown = false;
                }
                usleep(aimSmooth * 1000);
                continue;
            }
            //LOGI("TargetX : %f , TargetY : %f", TargetX, TargetY);
            tx += TargetX;
            ty += TargetY;
            if (isFiring)
                tx -= addCompensation;
            if (tx >= touchX + touchRange || tx <= touchX - touchRange || ty >= touchY + touchRange || ty <= touchY - touchRange) {
                usleep(1000);

                tx = touchX, ty = touchY;

                TouchInput::sendTouchUp();

                usleep(3000);

                if (!ChargingPortLeft)
                    TouchInput::sendTouchMove((int) tx, (int) ty);
                else
                    TouchInput::sendTouchMove(py * 2 - (float) tx, px * 2 - (float) ty);
                isDown = true;

                tx += TargetX;
                ty += TargetY;

                usleep(1000);
            }
            if (!ChargingPortLeft)
                TouchInput::sendTouchMove( (int) tx, (int) ty);
            else
                TouchInput::sendTouchMove( py * 2 - (float) tx, px * 2 - (float) ty);

            isDown = true;

            usleep(aimSmooth * 1000);
        } else {
            if (isDown) {
                tx = touchX, ty = touchY;
                TouchInput::sendTouchUp();
                isDown = false;
                usleep(aimSmooth * 1000);
            }
        }
        usleep(aimSmooth * 1000);
    }
}



